package com.uroria.backend.bukkit.twitch;

import com.uroria.backend.twitch.Subscriber;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class SubscriberUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final Subscriber subscriber;

    public SubscriberUpdateEvent(Subscriber subscriber) {
        super(true);
        this.subscriber = subscriber;
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
