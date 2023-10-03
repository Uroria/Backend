package com.uroria.backend.server.events;

import com.uroria.backend.server.Server;
import lombok.NonNull;

public abstract class ServerEvent {
    private final Server server;

    public ServerEvent(@NonNull Server server) {
        this.server = server;
    }

    public Server getServer() {
        return server;
    }
}
