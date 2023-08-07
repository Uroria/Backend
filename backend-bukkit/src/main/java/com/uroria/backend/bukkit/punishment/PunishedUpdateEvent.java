package com.uroria.backend.bukkit.punishment;

import com.uroria.backend.punishment.Punished;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PunishedUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final Punished punished;

    public PunishedUpdateEvent(Punished punished) {
        super(true);
        this.punished = punished;
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
