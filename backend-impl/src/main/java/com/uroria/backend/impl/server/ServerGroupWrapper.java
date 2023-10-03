package com.uroria.backend.impl.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.uroria.backend.Backend;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.NonNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ServerGroupWrapper implements ServerGroup {
    private final ServerManager serverManager;
    private final CommunicationWrapper object;
    private final String type;

    private boolean deleted;

    public ServerGroupWrapper(@NonNull CommunicationWrapper object, @NonNull ServerManager serverManager, @NonNull String type) {
        this.serverManager = serverManager;
        this.object = object;
        this.type = type;
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
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



        return Optional.empty();
    }

    @Override
    public Collection<Server> getServersWithTemplateId(int templateId) {
        return null;
    }

    @Override
    public Result<Server> createServer(int templateId) {

    }

    @Override
    public String getType() {
        return this.type;
    }

    @Override
    public Collection<User> getOnlineUsers() {
        return getRawOnlineUsers().stream()
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
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
}
