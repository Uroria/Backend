package com.uroria.backend.velocity.events;

import com.uroria.backend.common.player.BackendPlayer;

public final class PlayerUpdateEvent extends PlayerEvent {
    public PlayerUpdateEvent(BackendPlayer player) {
        super(player);
    }
}
