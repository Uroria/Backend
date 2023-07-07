package com.uroria.backend.pluginapi.events.server;

import com.uroria.backend.common.BackendServer;

public final class ServerStopEvent extends ServerEvent {
    public ServerStopEvent(BackendServer server) {
        super(server);
    }
}
