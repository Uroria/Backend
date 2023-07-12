package com.uroria.backend.player;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface PlayerManager {
    Optional<BackendPlayer> getPlayer(@NonNull UUID uuid, int timeout);

    Optional<BackendPlayer> getPlayer(@NonNull String name, int timeout);

    Optional<BackendPlayer> getPlayer(@NonNull UUID uuid);

    Optional<BackendPlayer> getPlayer(@NonNull String name);

    BackendPlayer updatePlayer(@NonNull BackendPlayer player);
}
