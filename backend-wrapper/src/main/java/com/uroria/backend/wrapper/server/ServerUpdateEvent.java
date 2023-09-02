package com.uroria.backend.wrapper.server;

import com.uroria.backend.server.Server;

public final class ServerUpdateEvent extends ServerEvent {

    public ServerUpdateEvent(Server server) {
        super(server);
    }
}
