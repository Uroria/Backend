package com.uroria.backend.proxy;

import com.uroria.backend.Deletable;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.server.Server;
import com.uroria.backend.user.User;
import lombok.NonNull;

import java.util.Collection;

public interface Proxy extends Deletable {
    long getIdentifier();

    String getName();

    Collection<User> getOnlineUsers();

    Collection<Server> getServers();

    void registerServer(Server server);

    void unregisterServer(Server server);

    ApplicationStatus getStatus();

    void setStatus(@NonNull ApplicationStatus status);

    int getOnlineUserCount();

    int getMaxUserCount();
}
