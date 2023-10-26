package com.uroria.backend;

import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;

import java.util.Optional;

public interface WrapperEnvironment {

    Optional<String> getServerGroupName();

    Optional<String> getProxyName();

    Optional<Integer> getTemplateId();

    Optional<Long> getProxyId();

    Optional<Proxy> getProxy();

    Optional<Long> getServerId();

    Optional<Server> getServer();
}
