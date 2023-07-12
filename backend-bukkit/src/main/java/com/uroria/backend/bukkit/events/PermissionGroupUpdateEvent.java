package com.uroria.backend.bukkit.events;

import com.uroria.backend.permission.PermissionGroup;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class PermissionGroupUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final PermissionGroup group;

    public PermissionGroupUpdateEvent(PermissionGroup group) {
        super(true);
        this.group = group;
    }

    public PermissionGroup getGroup() {
        return group;
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
