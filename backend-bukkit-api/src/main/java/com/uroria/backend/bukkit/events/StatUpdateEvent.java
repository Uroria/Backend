package com.uroria.backend.bukkit.events;

import com.uroria.backend.common.stats.BackendStat;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class StatUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final BackendStat stat;

    public StatUpdateEvent(BackendStat stat) {
        this.stat = stat;
    }

    public BackendStat getStat() {
        return stat;
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
