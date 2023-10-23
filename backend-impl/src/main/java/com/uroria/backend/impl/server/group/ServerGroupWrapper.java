package com.uroria.backend.impl.server.group;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uroria.backend.Backend;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.server.ServerManager;
import com.uroria.backend.impl.wrapper.Wrapper;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.NonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ServerGroupWrapper extends Wrapper implements ServerGroup {
    private final ServerManager serverManager;
    private final CommunicationWrapper object;
    private final String name;

    private boolean deleted;

    public ServerGroupWrapper(ServerManager serverManager, @NonNull CommunicationClient client, String name) {
        this.serverManager = serverManager;
        this.object = new CommunicationWrapper(name, client);
        this.name = name;
    }

    @Override
    public void refresh() {

    }

    @Override
    public JsonObject getObject() {
        return this.object.getObject();
    }

    @Override
    public CommunicationWrapper getObjectWrapper() {
        return this.object;
    }

    @Override
    public String getIdentifierKey() {
        return "name";
    }

    @Override
    public String getStringIdentifier() {
        return this.name;
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        getServers().forEach(server -> {
            server.setStatus(ApplicationStatus.STOPPED);
            server.delete();
        });
        object.set("deleted", true);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        Result<JsonElement> result = object.get("deleted");
        JsonElement element = result.get();
        if (element == null) return false;
        boolean val = element.getAsBoolean();
        if (val) this.deleted = true;
        return val;
    }

    @Override
    public Optional<Server> getServerByIdentifier(long identifier) {
        return getRawServers().stream()
                .filter(id -> id == identifier)
                .findAny()
                .map(id -> Backend.getServer(identifier).get());
    }

    @Override
    public Collection<Server> getServersWithTemplateId(int templateId) {
        return getRawServers().stream()
                .map(identifier -> Backend.getServer(identifier).get())
                .filter(Objects::nonNull)
                .filter(server -> server.getTemplateId() == templateId)
                .toList();
    }

    @Override
    public Collection<Server> getServers() {
        return getRawServers().stream()
                .map(identifier -> Backend.getServer(identifier).get())
                .filter(Objects::nonNull)
                .toList();
    }

    public void removeServer(long identifier) {

    }

    private Collection<Long> getRawServers() {
        Result<JsonElement> result = this.object.get("servers");
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray stringArray = element.getAsJsonArray();
        return stringArray.asList().stream()
                .map(JsonElement::getAsLong)
                .toList();
    }


    @Override
    public Result<Server> createServer(int templateId) {
        try {
            return Result.of(this.serverManager.createServerWrapper(templateId, getType(), getMaxUserCount()));
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public String getType() {
        return this.name;
    }

    @Override
    public Collection<User> getOnlineUsers() {
        return getRawOnlineUsers().stream()
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public int getOnlineUserCount() {
        Result<JsonElement> result = object.get("playerCount");
        JsonElement element = result.get();
        if (element == null) return 0;
        return element.getAsInt();
    }

    @Override
    public int getMaxUserCount() {
        Result<JsonElement> result = object.get("maxPlayerCount");
        JsonElement element = result.get();
        if (element == null) return 0;
        return element.getAsInt();
    }

    private Collection<UUID> getRawOnlineUsers() {
        Result<JsonElement> result = this.object.get("onlinePlayers");
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray uuidArray = element.getAsJsonArray();
        return uuidArray.asList().stream()
                .map(el -> UUID.fromString(el.getAsString()))
                .toList();
    }
}
