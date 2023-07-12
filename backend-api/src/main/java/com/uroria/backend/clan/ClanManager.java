package com.uroria.backend.clan;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface ClanManager {
    Optional<BackendClan> getClan(@NonNull String tag, int timeout);

    Optional<BackendClan> getClan(@NonNull UUID operator, int timeout);

    Optional<BackendClan> getClan(@NonNull String tag);

    Optional<BackendClan> getClan(@NonNull UUID operator);

    BackendClan updateClan(@NonNull BackendClan clan);
}
