package com.uroria.backend.velocity.events;

import com.uroria.backend.common.server.BackendServer;

public final class ServerStartEvent extends ServerEvent {
    public ServerStartEvent(BackendServer server) {
        super(server);
    }
}
