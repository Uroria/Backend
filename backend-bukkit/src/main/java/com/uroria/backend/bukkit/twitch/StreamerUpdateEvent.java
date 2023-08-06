package com.uroria.backend.bukkit.twitch;

import com.uroria.backend.twitch.Streamer;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class StreamerUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final Streamer streamer;

    public StreamerUpdateEvent(Streamer streamer) {
        super(true);
        this.streamer = streamer;
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
