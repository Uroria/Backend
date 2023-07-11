package com.uroria.backend.common.settings;

import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface SettingsManager {

    Collection<BackendSettings> getSettings(@NonNull UUID uuid, int gameId);

    Optional<BackendSettings> getSettings(@NonNull UUID uuid, int gameId, int id);

    Optional<BackendSettings> getSettings(@NonNull String tag);

    BackendSettings updateSettings(@NonNull BackendSettings settings);
}
