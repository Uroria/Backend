package com.uroria.backend.pluginapi.events.player;

import com.uroria.backend.pluginapi.events.Event;
import com.uroria.backend.common.player.BackendPlayer;

public abstract class PlayerEvent extends Event {
    private final BackendPlayer player;

    public PlayerEvent(BackendPlayer player) {
        this.player = player;
    }

    public BackendPlayer getPlayer() {
        return player;
    }
}
