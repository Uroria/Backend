package com.uroria.backend.api.events.player;

import com.uroria.backend.common.BackendPlayer;

public final class PlayerRegisterEvent extends PlayerEvent {
    public PlayerRegisterEvent(BackendPlayer player) {
        super(player);
    }
}
