package com.uroria.backend.velocity.events;

import com.uroria.backend.common.PermissionHolder;

public final class PermissionHolderUpdateEvent {
    private final PermissionHolder permissionHolder;

    public PermissionHolderUpdateEvent(PermissionHolder permissionHolder) {
        this.permissionHolder = permissionHolder;
    }

    public PermissionHolder getPermissionHolder() {
        return permissionHolder;
    }
}
