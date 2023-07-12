package com.uroria.backend.pluginapi.events.permission;

import com.uroria.backend.permission.PermissionGroup;

public final class PermissionGroupUpdateEvent extends PermissionGroupEvent {
    public PermissionGroupUpdateEvent(PermissionGroup permissionGroup) {
        super(permissionGroup);
    }
}
