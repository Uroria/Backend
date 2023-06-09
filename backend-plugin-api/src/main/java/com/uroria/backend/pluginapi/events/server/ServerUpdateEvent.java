package com.uroria.backend.pluginapi.events.server;

import com.uroria.backend.common.BackendServer;

public final class ServerUpdateEvent extends ServerEvent {
    public ServerUpdateEvent(BackendServer server) {
        super(server);
    }
}
