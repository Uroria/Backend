package com.uroria.backend.wrapper.permission;

import lombok.Getter;

public class HolderUpdateEvent {

    private @Getter final PermHolderOld holder;

    public HolderUpdateEvent(PermHolderOld holder) {
        this.holder = holder;
    }
}

