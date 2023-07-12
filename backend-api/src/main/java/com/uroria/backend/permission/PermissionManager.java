package com.uroria.backend.permission;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface PermissionManager {
    Optional<PermissionHolder> getHolder(@NonNull UUID uuid, int timeout);

    Optional<PermissionGroup> getGroup(@NonNull String name, int timeout);

    Optional<PermissionHolder> getHolder(@NonNull UUID uuid);

    Optional<PermissionGroup> getGroup(@NonNull String name);

    PermissionHolder updateHolder(@NonNull PermissionHolder holder);

    PermissionGroup updateGroup(@NonNull PermissionGroup group);
}
