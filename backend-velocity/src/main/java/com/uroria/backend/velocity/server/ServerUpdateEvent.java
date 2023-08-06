package com.uroria.backend.velocity.server;

import com.uroria.backend.server.Server;
import lombok.Getter;

public final class ServerUpdateEvent {

    private @Getter final Server server;

    public ServerUpdateEvent(Server server) {
        this.server = server;
    }
}
