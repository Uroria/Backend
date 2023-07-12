package com.uroria.backend.pluginapi.events.permission;

import com.uroria.backend.pluginapi.events.Event;
import com.uroria.backend.permission.PermissionGroup;

public abstract class PermissionGroupEvent extends Event {
    private final PermissionGroup permissionGroup;

    public PermissionGroupEvent(PermissionGroup permissionGroup) {
        this.permissionGroup = permissionGroup;
    }

    public PermissionGroup getPermissionGroup() {
        return permissionGroup;
    }
}
