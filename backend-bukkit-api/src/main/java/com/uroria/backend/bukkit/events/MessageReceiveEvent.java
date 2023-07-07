package com.uroria.backend.bukkit.events;

import com.uroria.backend.common.BackendMessage;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class MessageReceiveEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final BackendMessage message;

    public MessageReceiveEvent(BackendMessage message) {
        super(true);
        this.message = message;
    }

    public BackendMessage getMessage() {
        return message;
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
