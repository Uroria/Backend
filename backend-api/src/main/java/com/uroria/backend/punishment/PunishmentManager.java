package com.uroria.backend.punishment;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface PunishmentManager {
    default Optional<Punished> getPunished(UUID uuid) {
        return getPunished(uuid, 3000);
    }

    Optional<Punished> getPunished(UUID uuid, int timeout);

    void updatePunished(@NonNull Punished punished);
}
