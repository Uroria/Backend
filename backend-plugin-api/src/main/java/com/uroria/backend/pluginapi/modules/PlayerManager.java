package com.uroria.backend.pluginapi.modules;

import com.uroria.backend.common.BackendPlayer;

import java.util.Optional;
import java.util.UUID;

public interface PlayerManager {
    Optional<BackendPlayer> getPlayer(UUID uuid);
    Optional<BackendPlayer> getPlayer(String currentName);
    void updatePlayer(BackendPlayer player);
}
