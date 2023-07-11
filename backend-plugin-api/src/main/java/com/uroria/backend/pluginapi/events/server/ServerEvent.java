package com.uroria.backend.pluginapi.events.server;

import com.uroria.backend.common.server.BackendServer;
import com.uroria.backend.pluginapi.events.Event;

public abstract class ServerEvent extends Event {
    private final BackendServer server;

    public ServerEvent(BackendServer server) {
        this.server = server;
    }

    public BackendServer getServer() {
        return server;
    }
}
