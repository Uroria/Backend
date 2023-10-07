package com.uroria.backend.server.events;

import com.uroria.backend.server.Server;
import lombok.NonNull;

public final class ServerUpdatedEvent extends ServerEvent {
    public ServerUpdatedEvent(@NonNull Server server) {
        super(server);
    }
}
