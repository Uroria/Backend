package com.uroria.backend.clan;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface ClanManager {

    default Optional<Clan> getClan(String tag) {
        return getClan(tag, 3000);
    }

    default Optional<Clan> getClan(UUID operator) {
        return getClan(operator, 3000);
    }

    Optional<Clan> getClan(String tag, int timeout);

    Optional<Clan> getClan(UUID operator, int timeout);

    void updateClan(@NonNull Clan clan);
}
