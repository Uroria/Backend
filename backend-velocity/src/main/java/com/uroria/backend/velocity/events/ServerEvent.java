package com.uroria.backend.velocity.events;

import com.uroria.backend.server.BackendServer;

public abstract class ServerEvent {
    private final BackendServer server;

    public ServerEvent(BackendServer server) {
        this.server = server;
    }

    public BackendServer getServer() {
        return server;
    }
}
