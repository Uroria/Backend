package com.uroria.backend.pluginapi.events.permission;

import com.uroria.backend.common.permission.PermissionGroup;

public final class PermissionGroupCreateEvent extends PermissionGroupEvent {
    public PermissionGroupCreateEvent(PermissionGroup permissionGroup) {
        super(permissionGroup);
    }
}
