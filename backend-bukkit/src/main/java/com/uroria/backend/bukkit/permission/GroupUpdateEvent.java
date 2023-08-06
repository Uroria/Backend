package com.uroria.backend.bukkit.permission;

import com.uroria.backend.permission.PermGroup;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GroupUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final PermGroup group;

    public GroupUpdateEvent(PermGroup group) {
        super(true);
        this.group = group;
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
