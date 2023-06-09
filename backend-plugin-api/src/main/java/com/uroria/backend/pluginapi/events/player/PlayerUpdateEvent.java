package com.uroria.backend.pluginapi.events.player;

import com.uroria.backend.common.BackendPlayer;

public final class PlayerUpdateEvent extends PlayerEvent {
    public PlayerUpdateEvent(BackendPlayer player) {
        super(player);
    }
}