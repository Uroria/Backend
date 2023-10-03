package com.uroria.backend.permission.events;

import com.uroria.backend.permission.PermGroup;
import lombok.NonNull;

public final class GroupUpdatedEvent extends GroupEvent {
    public GroupUpdatedEvent(@NonNull PermGroup group) {
        super(group);
    }
}
