package com.uroria.backend.bukkit.permission;

import com.uroria.backend.permission.PermHolder;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class HolderUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final PermHolder holder;

    public HolderUpdateEvent(PermHolder holder) {
        super(true);
        this.holder = holder;
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

