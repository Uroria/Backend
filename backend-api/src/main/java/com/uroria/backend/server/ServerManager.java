package com.uroria.backend.server;

import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface ServerManager {

    default Optional<Server> getServer(long identifier) {
        return getServer(identifier, 3000);
    }

    default Optional<Server> getCloudServer(int id) {
        return getCloudServer(id, 3000);
    }

    Optional<Server> getServer(long identifier, int timeout);

    Optional<Server> getCloudServer(int id, int timeout);

    Server startServer(@NonNull Server server);

    void updateServer(@NonNull Server server);

    List<Server> getServers();
}
