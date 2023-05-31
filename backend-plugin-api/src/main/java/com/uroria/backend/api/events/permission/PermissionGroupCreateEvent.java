package com.uroria.backend.api.events.permission;

import com.uroria.backend.common.PermissionGroup;

public final class PermissionGroupCreateEvent extends PermissionGroupEvent {
    public PermissionGroupCreateEvent(PermissionGroup permissionGroup) {
        super(permissionGroup);
    }
}
