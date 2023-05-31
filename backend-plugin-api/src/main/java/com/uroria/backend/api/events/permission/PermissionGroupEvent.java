package com.uroria.backend.api.events.permission;

import com.uroria.backend.api.events.Event;
import com.uroria.backend.common.PermissionGroup;

public abstract class PermissionGroupEvent extends Event {
    private final PermissionGroup permissionGroup;

    public PermissionGroupEvent(PermissionGroup permissionGroup) {
        this.permissionGroup = permissionGroup;
    }

    public PermissionGroup getPermissionGroup() {
        return permissionGroup;
    }
}
