package com.uroria.backend.impl.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uroria.backend.Backend;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class ServerWrapper implements Server {
    private final CommunicationWrapper object;
    private final long identifier;

    private boolean deleted;
    private String type;
    private int templateId;
    private InetSocketAddress address;

    public ServerWrapper(@NonNull CommunicationClient client, long identifier) {
        this.object = new CommunicationWrapper(String.valueOf(identifier), client);
        this.identifier = identifier;
        this.templateId = -1;
    }

    public JsonObject getObject() {
        return this.object.getObject();
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
    public long getIdentifier() {
        return this.identifier;
    }

    @Override
    public void addProxy(@NonNull Proxy proxy) {
        Result<JsonElement> result = this.object.get("proxies");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        array.add(proxy.getIdentifier());
        this.object.set("proxies", element);
    }

    @Override
    public void removeProxy(Proxy proxy) {
        Result<JsonElement> result = this.object.get("proxies");
        JsonElement element = result.get();
        if (element == null) return;
        JsonArray array = element.getAsJsonArray();
        for (JsonElement value : array) {
            if (value.getAsLong() != proxy.getIdentifier()) continue;
            array.remove(value);
            break;
        }
        this.object.set("proxies", element);
    }

    @Override
    public Collection<Proxy> getProxies() {
        return getRawProxies().stream()
                .map(identifier -> Backend.getProxy(identifier).get())
                .filter(Objects::nonNull)
                .toList();
    }

    private Collection<Long> getRawProxies() {
        Result<JsonElement> result = this.object.get("proxies");
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray stringArray = element.getAsJsonArray();
        return stringArray.asList().stream()
                .map(JsonElement::getAsLong)
                .toList();
    }

    @Override
    public ServerGroup getGroup() {
        return Backend.getServerGroup(getType()).get();
    }

    @Override
    public ApplicationStatus getStatus() {
        Result<JsonElement> result = object.get("status");
        JsonElement element = result.get();
        if (element == null) return ApplicationStatus.EMPTY;
        return ApplicationStatus.getById(element.getAsInt());
    }

    @Override
    public void setStatus(@NonNull ApplicationStatus status) {
        this.object.set("status", status.getID());
    }

    @Override
    public Result<InetSocketAddress> getAddress() {
        if (this.address != null) return Result.some(this.address);
        Result<JsonElement> hostResult = object.get("host");
        Result<JsonElement> portResult = object.get("port");
        JsonElement hostElement = hostResult.get();
        JsonElement portElement = portResult.get();
        if (hostElement == null || portElement == null) return Result.none();
        InetSocketAddress address = new InetSocketAddress(hostElement.getAsString(), portElement.getAsInt());
        this.address = address;
        return Result.some(address);
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
        this.object.set("host", address.getHostName());
        this.object.set("port", address.getPort());
    }

    @Override
    public int getTemplateId() {
        if (this.templateId != -1) return this.templateId;
        Result<JsonElement> result = object.get("templateId");
        JsonElement element = result.get();
        if (element == null) return 0;
        int templateId = element.getAsInt();
        this.templateId = templateId;
        return templateId;
    }

    @Override
    public String getType() {
        if (this.type != null) return this.type;
        JsonElement name = this.object.get("type").get();
        if (name == null) return String.valueOf(identifier);
        String type = name.getAsString();
        this.type = type;
        return type;
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
