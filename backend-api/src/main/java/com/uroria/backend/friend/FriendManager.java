package com.uroria.backend.friend;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface FriendManager {

    default Optional<FriendHolder> getFriendHolder(UUID uuid) {
        return getFriendHolder(uuid, 3000);
    }

    Optional<FriendHolder> getFriendHolder(UUID uuid, int timeout);

    void updateFriendHolder(@NonNull FriendHolder holder);
}
