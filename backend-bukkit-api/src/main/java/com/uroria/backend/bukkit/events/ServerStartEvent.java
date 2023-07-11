package com.uroria.backend.bukkit.events;

import com.uroria.backend.common.server.BackendServer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class ServerStartEvent extends ServerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public ServerStartEvent(BackendServer server) {
        super(server);
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
