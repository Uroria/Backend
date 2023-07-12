package com.uroria.backend.pluginapi.events.permission;

import com.uroria.backend.permission.PermissionHolder;

public final class PermissionHolderUpdateEvent extends PermissionHolderEvent {
    public PermissionHolderUpdateEvent(PermissionHolder permissionHolder) {
        super(permissionHolder);
    }
}
