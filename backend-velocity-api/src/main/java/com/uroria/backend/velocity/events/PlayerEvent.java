package com.uroria.backend.velocity.events;

import com.uroria.backend.common.BackendPlayer;

public abstract class PlayerEvent {
    private final BackendPlayer player;

    PlayerEvent(BackendPlayer player) {
        this.player = player;
    }

    public BackendPlayer getPlayer() {
        return player;
    }
}
