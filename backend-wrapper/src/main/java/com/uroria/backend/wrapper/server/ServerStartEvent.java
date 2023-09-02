package com.uroria.backend.wrapper.server;

import com.uroria.backend.server.Server;

public final class ServerStartEvent extends ServerEvent {

    public ServerStartEvent(Server server) {
        super(server);
    }
}
