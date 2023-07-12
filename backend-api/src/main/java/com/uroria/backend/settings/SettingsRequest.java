package com.uroria.backend.settings;

import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public final class SettingsRequest implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private final int gameId;
    private final int id;
    private final String tag;

    public SettingsRequest(@NonNull UUID uuid, int gameId) {
        this(uuid, gameId, -1);
    }

    public SettingsRequest(@NonNull UUID uuid, int gameId, int id) {
        this.uuid = uuid;
        this.gameId = gameId;
        this.id = id;
        this.tag = null;
    }

    public SettingsRequest(@NonNull String tag) {
        this.uuid = null;
        this.gameId = -1;
        this.id = -1;
        this.tag = tag;
    }

    public Optional<Integer> getGameID() {
        if (this.gameId == -1) return Optional.empty();
        return Optional.of(this.gameId);
    }

    public Optional<Integer> getID() {
        if (this.id == -1) return Optional.empty();
        return Optional.of(this.id);
    }

    public Optional<String> getTag() {
        return Optional.ofNullable(this.tag);
    }

    public Optional<UUID> getUUID() {
        return Optional.ofNullable(this.uuid);
    }
}
