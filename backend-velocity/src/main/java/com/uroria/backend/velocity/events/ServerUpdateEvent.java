package com.uroria.backend.velocity.events;

import com.uroria.backend.server.BackendServer;

public final class ServerUpdateEvent extends ServerEvent {

    public ServerUpdateEvent(BackendServer server) {
        super(server);
    }
}
