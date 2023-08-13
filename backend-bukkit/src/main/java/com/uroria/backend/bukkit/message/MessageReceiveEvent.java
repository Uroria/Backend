package com.uroria.backend.bukkit.message;

import com.uroria.backend.message.Message;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MessageReceiveEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final Message message;

    public MessageReceiveEvent(Message message) {
        super(true);
        this.message = message;
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
