package com.uroria.backend.wrapper.server;

import com.uroria.backend.server.Server;

public final class ServerCloseEvent extends ServerEvent {

    public ServerCloseEvent(Server server) {
        super(server);
    }
}
