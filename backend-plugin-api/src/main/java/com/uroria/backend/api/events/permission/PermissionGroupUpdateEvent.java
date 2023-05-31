package com.uroria.backend.api.events.permission;

import com.uroria.backend.common.PermissionGroup;

public final class PermissionGroupUpdateEvent extends PermissionGroupEvent {
    public PermissionGroupUpdateEvent(PermissionGroup permissionGroup) {
        super(permissionGroup);
    }
}
