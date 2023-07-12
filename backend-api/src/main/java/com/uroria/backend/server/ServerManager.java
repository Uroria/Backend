package com.uroria.backend.server;

import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;

public interface ServerManager {

    Optional<BackendServer> getServer(int id, int timeout);

    Optional<BackendServer> getServer(int id);

    BackendServer updateServer(@NonNull BackendServer server);

    BackendServer startServer(@NonNull BackendServer server);

    Collection<BackendServer> getServers();
}
