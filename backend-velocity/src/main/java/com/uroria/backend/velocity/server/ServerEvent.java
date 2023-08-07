package com.uroria.backend.velocity.server;

import com.uroria.backend.server.Server;
import lombok.Getter;

public abstract class ServerEvent {
    private @Getter final Server server;

    public ServerEvent(Server server) {
        this.server = server;
    }
}
