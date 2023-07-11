package com.uroria.backend.common.friends;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface FriendManager {

    Optional<BackendFriend> getFriend(@NonNull UUID uuid, int timeout);

    Optional<BackendFriend> getFriend(@NonNull UUID uuid);

    BackendFriend updateFriend(@NonNull BackendFriend friend);
}
