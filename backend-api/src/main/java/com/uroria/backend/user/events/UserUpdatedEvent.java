package com.uroria.backend.user.events;

import com.uroria.backend.user.User;

public final class UserUpdatedEvent extends UserEvent {
    public UserUpdatedEvent(User user) {
        super(user);
    }
}
