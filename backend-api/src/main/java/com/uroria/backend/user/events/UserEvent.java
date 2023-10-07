package com.uroria.backend.user.events;

import com.uroria.backend.user.User;
import lombok.NonNull;

public abstract class UserEvent {
    private final User user;

    public UserEvent(@NonNull User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
