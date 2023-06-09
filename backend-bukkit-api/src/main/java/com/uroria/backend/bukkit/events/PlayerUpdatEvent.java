package com.uroria.backend.bukkit.events;

import com.uroria.backend.common.BackendPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PlayerUpdatEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final BackendPlayer player;

    public PlayerUpdatEvent(BackendPlayer player) {
        this.player = player;
    }

    public BackendPlayer getPlayer() {
        return player;
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
