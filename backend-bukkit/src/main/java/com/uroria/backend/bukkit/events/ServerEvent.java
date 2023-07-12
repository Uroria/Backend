package com.uroria.backend.bukkit.events;

import com.uroria.backend.server.BackendServer;
import org.bukkit.event.Event;

public abstract class ServerEvent extends Event {
    private final BackendServer server;

    public ServerEvent(BackendServer server) {
        super(true);
        this.server = server;
    }

    public BackendServer getServer() {
        return server;
    }
}
