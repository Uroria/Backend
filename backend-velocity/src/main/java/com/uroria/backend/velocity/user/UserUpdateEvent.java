package com.uroria.backend.velocity.user;

import com.uroria.backend.user.User;
import lombok.Getter;

public final class UserUpdateEvent {

    private @Getter final User user;

    public UserUpdateEvent(User user) {
        this.user = user;
    }
}
