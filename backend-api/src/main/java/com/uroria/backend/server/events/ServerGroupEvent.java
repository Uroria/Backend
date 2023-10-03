package com.uroria.backend.server.events;

import com.uroria.backend.server.ServerGroup;
import lombok.NonNull;

public abstract class ServerGroupEvent {
    private final ServerGroup group;

    public ServerGroupEvent(@NonNull ServerGroup group) {
        this.group = group;
    }

    public ServerGroup getGroup() {
        return group;
    }
}
