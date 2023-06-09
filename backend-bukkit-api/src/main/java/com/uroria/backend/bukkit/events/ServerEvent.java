package com.uroria.backend.bukkit.events;

import com.uroria.backend.common.BackendServer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class ServerEvent extends Event {
    private final BackendServer server;

    public ServerEvent(BackendServer server) {
        this.server = server;
    }

    public BackendServer getServer() {
        return server;
    }
}
