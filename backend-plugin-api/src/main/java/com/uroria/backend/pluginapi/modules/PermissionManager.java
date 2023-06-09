package com.uroria.backend.pluginapi.modules;

import com.uroria.backend.common.PermissionGroup;
import com.uroria.backend.common.PermissionHolder;

import java.util.Optional;
import java.util.UUID;

public interface PermissionManager {
    Optional<PermissionGroup> getGroup(String name);
    Optional<PermissionHolder> getHolder(UUID uuid);
    void updateGroup(PermissionGroup group);
    void updateHolder(PermissionHolder holder);
}
