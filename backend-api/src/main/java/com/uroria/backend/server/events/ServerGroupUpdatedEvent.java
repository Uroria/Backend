package com.uroria.backend.server.events;

import com.uroria.backend.server.ServerGroup;
import lombok.NonNull;

public final class ServerGroupUpdatedEvent extends ServerGroupEvent {
    public ServerGroupUpdatedEvent(@NonNull ServerGroup group) {
        super(group);
    }
}
