package com.uroria.backend.wrapper.user;

import lombok.Getter;

public final class UserUpdateEvent {

    private @Getter final UserOld user;

    public UserUpdateEvent(UserOld user) {
        this.user = user;
    }
}
