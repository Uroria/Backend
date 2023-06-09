package com.uroria.backend.api.modules;

import com.uroria.backend.common.BackendServer;

import java.util.Optional;

public interface ServerManager {
    Optional<BackendServer> getServer(int id);

    void updateServer(BackendServer server);

    BackendServer startServer(BackendServer server);
}
