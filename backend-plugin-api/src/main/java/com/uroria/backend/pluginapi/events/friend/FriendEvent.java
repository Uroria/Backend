package com.uroria.backend.pluginapi.events.friend;

import com.uroria.backend.common.friends.BackendFriend;
import com.uroria.backend.pluginapi.events.Event;

public abstract class FriendEvent extends Event {
    private final BackendFriend friend;

    public FriendEvent(BackendFriend friend) {
        this.friend = friend;
    }

    public BackendFriend getFriend() {
        return friend;
    }
}
