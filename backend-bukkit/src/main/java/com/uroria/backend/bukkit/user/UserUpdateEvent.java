package com.uroria.backend.bukkit.user;

import com.uroria.backend.user.User;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class UserUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private @Getter final User user;

    public UserUpdateEvent(User user) {
        super(true);
        this.user = user;
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
