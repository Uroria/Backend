package com.uroria.backend.wrapper.permission;

import com.uroria.backend.permission.PermGroup;
import lombok.Getter;

public class GroupUpdateEvent {

    private @Getter final PermGroup group;

    public GroupUpdateEvent(PermGroup group) {
        this.group = group;
    }
}
