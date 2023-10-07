package com.uroria.backend.user.events;

import com.uroria.backend.user.User;
import lombok.NonNull;

public final class UserDeletedEvent extends UserEvent {
    public UserDeletedEvent(@NonNull User user) {
        super(user);
    }
}
