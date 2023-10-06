package com.uroria.backend.impl.server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uroria.backend.Backend;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.wrapper.Wrapper;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.user.User;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

public final class ServerWrapper extends Wrapper implements Server {
    private final CommunicationWrapper object;
    private final long identifier;
    private final ServerGroupWrapper group;

    private boolean deleted;
    private String type;
    private int templateId;
    private InetSocketAddress address;

    public ServerWrapper(@NonNull CommunicationClient client, long identifier) {
        this.object = new CommunicationWrapper(String.valueOf(identifier), client);
        this.identifier = identifier;
        this.templateId = -1;
        this.group = getGroup();
    }

    public ServerWrapper(@NonNull CommunicationClient client, long identifier, ServerGroupWrapper group) {
        this.object = new CommunicationWrapper(String.valueOf(identifier), client);
        this.identifier = identifier;
        this.templateId = -1;
        this.group = group;
    }

    @Override
    public CommunicationWrapper getObjectWrapper() {
        return this.object;
    }

    @Override
    public void refresh() {

    }

    @Override
    public JsonObject getObject() {
        return this.object.getObject();
    }

    @Override
    public String getIdentifierKey() {
        return String.valueOf(identifier);
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
        boolean val = getBoolean("deleted", false);
        if (val) this.deleted = true;
        return val;
    }

    @Override
    public long getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getStringIdentifier() {
        return String.valueOf(this.identifier);
    }

    @Override
    public void addProxy(@NonNull Proxy proxy) {
        addToLongList("proxies", proxy.getIdentifier());
    }

    @Override
    public void removeProxy(Proxy proxy) {
        removeFromLongList("proxies", proxy.getIdentifier());
    }

    @Override
    public Collection<Proxy> getProxies() {
        return getLongs("proxies").stream()
                .map(identifier -> Backend.getProxy(identifier).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public ServerGroupWrapper getGroup() {
        return (ServerGroupWrapper) Backend.getServerGroup(getType()).get();
    }

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.getById(getInt("status"));
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
        int templateId = getInt("templateId", -1);
        this.templateId = templateId;
        return templateId;
    }

    @Override
    public String getType() {
        if (this.type != null) return this.type;
        this.type = getString("type", String.valueOf(identifier));
        return this.type;
    }

    @Override
    public Collection<User> getOnlineUsers() {
        return getStrings("onlinePlayers").stream()
                .map(UUID::fromString)
                .map(uuid -> Backend.getUser(uuid).get())
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public int getOnlineUserCount() {
        return getInt("playerCount");
    }

    @Override
    public int getMaxUserCount() {
        return getInt("maxPlayerCount");
    }
}
