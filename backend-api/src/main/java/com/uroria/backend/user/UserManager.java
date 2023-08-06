package com.uroria.backend.user;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface UserManager {

    default Optional<User> getUser(UUID uuid) {
        return getUser(uuid, 3000);
    }

    Optional<User> getUser(UUID uuid, int timeout);

    default Optional<User> getUser(String name) {
        return getUser(name, 3000);
    }

    Optional<User> getUser(String name, int timeout);

    void updateUser(@NonNull User user);
}
