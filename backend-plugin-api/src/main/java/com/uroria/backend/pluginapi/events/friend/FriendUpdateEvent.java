package com.uroria.backend.pluginapi.events.friend;

import com.uroria.backend.friends.BackendFriend;

public final class FriendUpdateEvent extends FriendEvent {

    public FriendUpdateEvent(BackendFriend friend) {
        super(friend);
    }
}
