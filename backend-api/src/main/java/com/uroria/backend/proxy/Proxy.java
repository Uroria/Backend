package com.uroria.backend.proxy;

import com.uroria.backend.Deletable;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.server.Server;
import com.uroria.backend.user.User;
import lombok.NonNull;

import java.util.Collection;

public interface Proxy extends Deletable {
    long getId();

    String getName();

    Collection<User> getOnlineUsers();

    Collection<Server> getServers();

    void registerServer(Server server);

    void unregisterServer(Server server);

    default void start() {
        if (getStatus() != ApplicationStatus.EMPTY) return;
        setStatus(ApplicationStatus.STARTING);
    }

    default void stop() {
        switch (getStatus()) {
            case ONLINE, CLOSED, STARTING -> {
                setStatus(ApplicationStatus.STOPPED);
            }
        }
    }

    default void lock() {
        if (getStatus() != ApplicationStatus.ONLINE) return;
        setStatus(ApplicationStatus.CLOSED);
    }

    default void unlock() {
        if (getStatus() != ApplicationStatus.CLOSED) return;
        setStatus(ApplicationStatus.ONLINE);
    }

    ApplicationStatus getStatus();

    void setStatus(@NonNull ApplicationStatus status);

    int getOnlineUserCount();

    int getMaxUserCount();
}
