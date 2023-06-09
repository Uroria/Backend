package com.uroria.backend.bukkit.events;

import com.uroria.backend.common.PermissionHolder;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PermissionHolderUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final PermissionHolder holder;

    public PermissionHolderUpdateEvent(PermissionHolder holder) {
        this.holder = holder;
    }

    public PermissionHolder getHolder() {
        return holder;
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
