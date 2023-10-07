package com.uroria.backend.impl.proxy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.wrapper.Wrapper;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.user.User;
import lombok.NonNull;

import java.util.Collection;

public final class ProxyWrapper extends Wrapper implements Proxy {
    private final CommunicationWrapper object;
    private final long identifier;

    private boolean deleted;

    public ProxyWrapper(@NonNull CommunicationClient client, long identifier) {
        this.object = new CommunicationWrapper(String.valueOf(identifier), client);
        this.identifier = identifier;
    }

    @Override
    public long getIdentifier() {
        return this.identifier;
    }

    @Override
    public String getName() {
        JsonElement name = this.object.get("name").get();
        if (name == null) return String.valueOf(identifier);
        return name.getAsString();
    }

    @Override
    public Collection<User> getOnlineUsers() {
        return null;
    }

    @Override
    public Collection<Server> getServers() {
        return null;
    }

    @Override
    public void registerServer(Server server) {

    }

    @Override
    public void unregisterServer(Server server) {

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
    public int getOnlineUserCount() {
        return getInt("playerCount");
    }

    @Override
    public int getMaxUserCount() {
        return getInt("maxPlayerCount");
    }

    @Override
    public void delete() {
        if (this.deleted) return;
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
        return String.valueOf(this.identifier);
    }

    @Override
    public String getStringIdentifier() {
        return "identifier";
    }
}
