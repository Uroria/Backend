package com.uroria.backend.server;

import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.proxy.Proxy;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.net.InetSocketAddress;
import java.util.Collection;

public interface Server extends ServerGroupTarget {

    long getIdentifier();

    void addProxy(@NonNull Proxy proxy);

    void removeProxy(Proxy proxy);

    Collection<Proxy> getProxies();

    ServerGroup getGroup();

    ApplicationStatus getStatus();

    void setStatus(@NonNull ApplicationStatus status);

    Result<InetSocketAddress> getAddress();

    int getTemplateId();
}
