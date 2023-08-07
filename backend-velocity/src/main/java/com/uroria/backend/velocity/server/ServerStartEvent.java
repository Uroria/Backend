package com.uroria.backend.velocity.server;

import com.uroria.backend.server.Server;

public final class ServerStartEvent extends ServerEvent {

    public ServerStartEvent(Server server) {
        super(server);
    }
}
