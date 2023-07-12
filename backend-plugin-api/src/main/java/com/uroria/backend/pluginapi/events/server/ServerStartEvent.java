package com.uroria.backend.pluginapi.events.server;

import com.uroria.backend.server.BackendServer;

public final class ServerStartEvent extends ServerEvent {
    public ServerStartEvent(BackendServer server) {
        super(server);
    }
}
