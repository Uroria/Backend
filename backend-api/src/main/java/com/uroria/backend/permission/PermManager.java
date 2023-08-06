package com.uroria.backend.permission;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface PermManager {

    default Optional<PermHolder> getHolder(UUID uuid) {
        return getHolder(uuid, 3000);
    }

    default Optional<PermGroup> getGroup(String name) {
        return getGroup(name, 3000);
    }

    Optional<PermHolder> getHolder(UUID uuid, int timeout);

    Optional<PermGroup> getGroup(String name, int timeout);

    void updateHolder(@NonNull PermHolder holder);

    void updateGroup(@NonNull PermGroup group);
}
