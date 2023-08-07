package com.uroria.backend.velocity.friend;

import com.uroria.backend.friend.FriendHolder;
import lombok.Getter;

public final class FriendUpdateEvent {

    private @Getter final FriendHolder friend;

    public FriendUpdateEvent(FriendHolder friend) {
        this.friend = friend;
    }
}
