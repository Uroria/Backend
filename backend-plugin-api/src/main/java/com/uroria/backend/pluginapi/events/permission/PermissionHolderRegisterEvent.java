package com.uroria.backend.pluginapi.events.permission;

import com.uroria.backend.common.permission.PermissionHolder;

public final class PermissionHolderRegisterEvent extends PermissionHolderEvent {
    public PermissionHolderRegisterEvent(PermissionHolder permissionHolder) {
        super(permissionHolder);
    }
}
