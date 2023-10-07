package com.uroria.backend.server.events;

import com.uroria.backend.server.ServerGroup;
import lombok.NonNull;

public final class ServerGroupDeletedEvent extends ServerGroupEvent {
    public ServerGroupDeletedEvent(@NonNull ServerGroup group) {
        super(group);
    }
}
