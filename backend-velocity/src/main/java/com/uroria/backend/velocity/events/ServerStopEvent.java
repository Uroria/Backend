package com.uroria.backend.velocity.events;

import com.uroria.backend.server.BackendServer;

public final class ServerStopEvent extends ServerEvent {
    public ServerStopEvent(BackendServer server) {
        super(server);
    }
}