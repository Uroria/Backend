package com.uroria.backend.wrapper.server;

public final class ServerUpdateEvent extends ServerEvent {

    public ServerUpdateEvent(Serverold server) {
        super(server);
    }
}
