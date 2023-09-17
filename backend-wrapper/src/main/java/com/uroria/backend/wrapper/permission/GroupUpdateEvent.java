package com.uroria.backend.wrapper.permission;

import lombok.Getter;

public class GroupUpdateEvent {

    private @Getter final PermGroupOld group;

    public GroupUpdateEvent(PermGroupOld group) {
        this.group = group;
    }
}
