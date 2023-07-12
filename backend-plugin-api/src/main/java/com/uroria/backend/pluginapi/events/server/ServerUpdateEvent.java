package com.uroria.backend.pluginapi.events.server;

import com.uroria.backend.server.BackendServer;

public final class ServerUpdateEvent extends ServerEvent {
    public ServerUpdateEvent(BackendServer server) {
        super(server);
    }
}
