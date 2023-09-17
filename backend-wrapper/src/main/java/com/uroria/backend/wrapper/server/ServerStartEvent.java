package com.uroria.backend.wrapper.server;

public final class ServerStartEvent extends ServerEvent {

    public ServerStartEvent(Serverold server) {
        super(server);
    }
}
