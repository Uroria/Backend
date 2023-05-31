package com.uroria.backend.api.events.permission;

import com.uroria.backend.api.events.Event;
import com.uroria.backend.common.PermissionHolder;

public abstract class PermissionHolderEvent extends Event {
    private final PermissionHolder permissionHolder;

    public PermissionHolderEvent(PermissionHolder permissionHolder) {
        this.permissionHolder = permissionHolder;
    }

    public PermissionHolder getPermissionHolder() {
        return permissionHolder;
    }
}
