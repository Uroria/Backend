package com.uroria.backend.wrapper.server;

public final class ServerCloseEvent extends ServerEvent {

    public ServerCloseEvent(Serverold server) {
        super(server);
    }
}
