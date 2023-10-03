package com.uroria.backend.server.events;

import com.uroria.backend.server.Server;
import lombok.NonNull;

public final class ServerDeletedEvent extends ServerEvent {
    public ServerDeletedEvent(@NonNull Server server) {
        super(server);
    }
}
