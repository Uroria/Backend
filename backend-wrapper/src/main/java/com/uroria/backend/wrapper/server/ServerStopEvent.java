package com.uroria.backend.wrapper.server;

public final class ServerStopEvent extends ServerEvent {

    public ServerStopEvent(Serverold server) {
        super(server);
    }
}
