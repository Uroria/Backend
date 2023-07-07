package com.uroria.backend.pluginapi.events.server;

import com.uroria.backend.common.BackendServer;

public final class ServerStartEvent extends ServerEvent {
    public ServerStartEvent(BackendServer server) {
        super(server);
    }
}
