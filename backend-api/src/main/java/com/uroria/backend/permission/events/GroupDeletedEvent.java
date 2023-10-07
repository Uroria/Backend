package com.uroria.backend.permission.events;

import com.uroria.backend.permission.PermGroup;
import lombok.NonNull;

public final class GroupDeletedEvent extends GroupEvent {
    public GroupDeletedEvent(@NonNull PermGroup group) {
        super(group);
    }
}
