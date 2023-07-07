package com.uroria.backend.pluginapi.modules;

import com.uroria.backend.common.BackendServer;

import java.util.List;
import java.util.Optional;

public interface ServerManager {
    Optional<BackendServer> getServer(int id);

    void updateServer(BackendServer server);

    BackendServer startServer(BackendServer server);

    List<BackendServer> getAllServers();
}
