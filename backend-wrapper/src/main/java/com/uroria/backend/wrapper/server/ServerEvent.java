package com.uroria.backend.wrapper.server;

import lombok.Getter;

public abstract class ServerEvent {
    private @Getter final Serverold server;

    public ServerEvent(Serverold server) {
        this.server = server;
    }
}
