package com.uroria.backend.pluginapi.events.player;

import com.uroria.backend.common.BackendPlayer;

public final class PlayerRegisterEvent extends PlayerEvent {
    public PlayerRegisterEvent(BackendPlayer player) {
        super(player);
    }
}