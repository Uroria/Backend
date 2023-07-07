package com.uroria.backend.pluginapi.modules;

import com.uroria.backend.common.BackendSettings;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SettingsManager {

    Collection<BackendSettings> getSettings(UUID uuid, int gameId);

    Optional<BackendSettings> getSettings(UUID uuid, int gameId, int id);

    Optional<BackendSettings> getSettings(String tag);

    void updateSettings(BackendSettings settings);
}
