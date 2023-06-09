package com.uroria.backend.velocity.events;

import com.uroria.backend.common.BackendServer;

public final class ServerUpdateEvent extends ServerEvent {

    public ServerUpdateEvent(BackendServer server) {
        super(server);
    }
}
