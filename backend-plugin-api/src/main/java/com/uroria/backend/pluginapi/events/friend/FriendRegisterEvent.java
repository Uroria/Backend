package com.uroria.backend.pluginapi.events.friend;

import com.uroria.backend.common.BackendFriend;

public final class FriendRegisterEvent extends FriendEvent {
    public FriendRegisterEvent(BackendFriend friend) {
        super(friend);
    }
}
