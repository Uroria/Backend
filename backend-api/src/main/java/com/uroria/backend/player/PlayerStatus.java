package com.uroria.backend.player;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public enum PlayerStatus {
    OFFLINE(0),
    ONLINE(1),
    DO_NOT_DISTURB(2),
    LIVE(3);
    private @Getter final int id;
    PlayerStatus(int id) {
        this.id = id;
    }

    public static @Nullable PlayerStatus getById(int id) {
        for (PlayerStatus status : values()) {
            if (status.getId() == id) return status;
        }
        return null;
    }
}
