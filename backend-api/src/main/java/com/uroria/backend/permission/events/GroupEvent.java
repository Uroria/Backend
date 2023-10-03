package com.uroria.backend.permission.events;

import com.uroria.backend.permission.PermGroup;
import lombok.NonNull;

public abstract class GroupEvent {
    private final PermGroup group;

    public GroupEvent(@NonNull PermGroup group) {
        this.group = group;
    }

    public PermGroup getGroup() {
        return group;
    }
}
