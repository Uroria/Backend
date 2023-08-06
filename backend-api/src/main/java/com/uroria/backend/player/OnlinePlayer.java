package com.uroria.backend.player;

import com.uroria.backend.property.PropertyObject;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public final class OnlinePlayer extends PropertyObject<OnlinePlayer> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private final String name;

    public OnlinePlayer(@NonNull UUID uuid, @NonNull String name) {
        this.uuid = uuid;
        this.name = name.toLowerCase();
    }

    @Override
    public void modify(OnlinePlayer player) {
        this.deleted = player.deleted;
    }

    @Override
    public void update() {

    }
}
