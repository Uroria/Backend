package com.uroria.backend.velocity.events;

import com.uroria.backend.common.PermissionGroup;

public final class PermissionGroupUpdateEvent {
    private final PermissionGroup group;

    public PermissionGroupUpdateEvent(PermissionGroup group) {
        this.group = group;
    }

    public PermissionGroup getGroup() {
        return group;
    }
}
