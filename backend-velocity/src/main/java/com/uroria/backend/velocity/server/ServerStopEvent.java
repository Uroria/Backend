package com.uroria.backend.velocity.server;

import com.uroria.backend.server.Server;

public final class ServerStopEvent extends ServerEvent {

    public ServerStopEvent(Server server) {
        super(server);
    }
}
