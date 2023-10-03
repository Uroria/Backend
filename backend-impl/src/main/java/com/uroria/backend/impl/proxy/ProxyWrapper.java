package com.uroria.backend.impl.proxy;

import com.google.gson.JsonElement;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.user.User;
import lombok.NonNull;

import java.util.Collection;

public final class ProxyWrapper implements Proxy {
    private final CommunicationWrapper object;
    private final long identifier;

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
        return null;
    }

    @Override
    public void setStatus(@NonNull ApplicationStatus status) {

    }

    @Override
    public int getOnlineUserCount() {
        return 0;
    }

    @Override
    public int getMaxUserCount() {
        return 0;
    }

    @Override
    public void delete() {

    }

    @Override
    public boolean isDeleted() {
        return false;
    }
}
