package com.uroria.backend.server;

import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.proxy.Proxy;
import com.uroria.base.property.PropertyObject;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface Server extends ServerGroupTarget, PropertyObject {

    long getId();

    void addProxy(@NonNull Proxy proxy);

    void removeProxy(Proxy proxy);

    Collection<Proxy> getProxies();

    ServerGroup getGroup();

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

    Result<InetSocketAddress> getAddress();

    int getTemplateId();
}
