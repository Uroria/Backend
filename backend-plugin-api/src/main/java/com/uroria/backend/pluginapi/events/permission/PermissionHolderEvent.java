package com.uroria.backend.pluginapi.events.permission;

import com.uroria.backend.pluginapi.events.Event;
import com.uroria.backend.common.permission.PermissionHolder;

public abstract class PermissionHolderEvent extends Event {
    private final PermissionHolder permissionHolder;

    public PermissionHolderEvent(PermissionHolder permissionHolder) {
        this.permissionHolder = permissionHolder;
    }

    public PermissionHolder getPermissionHolder() {
        return permissionHolder;
    }
}
