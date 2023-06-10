package com.uroria.backend.pluginapi.modules;

import com.uroria.backend.common.BackendFriend;

import java.util.Optional;
import java.util.UUID;

public interface FriendManager {
    Optional<BackendFriend> getFriend(UUID friend);
    void updateFriend(BackendFriend friend);
}
