package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public final class BackendStat extends PropertyHolder implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID uuid;
    private final int gameId;
    public BackendStat(UUID uuid, int gameId) {
        this.uuid = uuid;
        this.gameId = gameId;
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getGameId() {
        return gameId;
    }
}
