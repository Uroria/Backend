package com.uroria.backend.api.events.player;

import com.uroria.backend.api.events.Event;
import com.uroria.backend.common.BackendPlayer;

public abstract class PlayerEvent extends Event {
    private final BackendPlayer player;

    public PlayerEvent(BackendPlayer player) {
        this.player = player;
    }

    public BackendPlayer getPlayer() {
        return player;
    }
}
