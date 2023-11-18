package com.uroria.backend.impl;

import com.uroria.backend.Backend;
import com.uroria.backend.WrapperEnvironment;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import lombok.Setter;

import java.util.Optional;

public final class BackendEnvironment implements WrapperEnvironment {
    @Setter private String serverGroupName;
    @Setter private String proxyGroupName;
    @Setter private int templateId;
    @Setter private long proxyId;
    @Setter private long serverId;

    @Override
    public Optional<String> getServerGroupName() {
        return Optional.ofNullable(this.serverGroupName);
    }

    @Override
    public Optional<String> getProxyName() {
        return Optional.ofNullable(this.proxyGroupName);
    }

    @Override
    public Optional<Integer> getTemplateId() {
        if (templateId == 0) return Optional.empty();
        return Optional.of(templateId);
    }

    @Override
    public Optional<Long> getProxyId() {
        if (proxyId == 0) return Optional.empty();
        return Optional.of(proxyId);
    }

    @Override
    public Optional<Proxy> getProxy() {
        return getProxyId().map(id -> Backend.proxy(id).get());
    }

    @Override
    public Optional<Long> getServerId() {
        if (serverId == 0) return Optional.empty();
        return Optional.of(serverId);
    }

    @Override
    public Optional<Server> getServer() {
        return getServerId().map(id -> Backend.server(id).get());
    }
}
