package com.uroria.backend.wrapper.permission;

import com.uroria.backend.permission.PermHolder;
import lombok.Getter;

public class HolderUpdateEvent {

    private @Getter final PermHolder holder;

    public HolderUpdateEvent(PermHolder holder) {
        this.holder = holder;
    }
}

