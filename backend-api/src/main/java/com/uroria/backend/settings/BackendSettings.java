package com.uroria.backend.settings;

import com.uroria.backend.helpers.PropertyHolder;
import com.uroria.backend.utils.ObjectUtils;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public final class BackendSettings extends PropertyHolder<BackendSettings> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private final int gameId;
    private final int id;
    private final String tag;
    private String displayName;
    private boolean deleted;

    public BackendSettings(@NonNull UUID uuid, int gameId, int id, @NonNull String displayName) {
        this.uuid = uuid;
        this.gameId = gameId;
        this.id = id;
        this.tag = uuid + "-" + gameId + "-" + id;
        this.displayName = displayName;
    }

    @Override
    public void modify(BackendSettings settings) {
        ObjectUtils.overrideMap(properties, settings.properties);
        this.displayName = settings.displayName;
        this.deleted = settings.deleted;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    public String getTag() {
        return tag;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void delete() {
        this.deleted = true;
    }

    public void setDeleted(boolean value) {
        this.deleted = value;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public int getID() {
        return this.id;
    }

    public int getGameID() {
        return this.gameId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof BackendSettings settings) {
            if (!settings.getUUID().equals(this.uuid)) return false;
            if (settings.id != this.id) return false;
            return settings.gameId == this.gameId;
        }
        return false;
    }
}
