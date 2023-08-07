package com.uroria.backend.bukkit.server;

import com.uroria.backend.server.Server;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class ServerUpdateEvent extends ServerEvent {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    public ServerUpdateEvent(Server server) {
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
