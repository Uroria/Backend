package com.uroria.backend.api.events.permission;

import com.uroria.backend.common.PermissionHolder;

public final class PermissionHolderRegisterEvent extends PermissionHolderEvent {
    public PermissionHolderRegisterEvent(PermissionHolder permissionHolder) {
        super(permissionHolder);
    }
}
