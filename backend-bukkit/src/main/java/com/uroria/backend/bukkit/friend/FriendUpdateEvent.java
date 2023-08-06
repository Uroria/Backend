package com.uroria.backend.bukkit.friend;

import com.uroria.backend.friend.FriendHolder;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class FriendUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final FriendHolder friend;

    public FriendUpdateEvent(FriendHolder friend) {
        super(true);
        this.friend = friend;
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

