package com.uroria.backend.pluginapi.modules;

import com.uroria.backend.common.BackendClan;

import java.util.Optional;
import java.util.UUID;

public interface ClanManger {
    Optional<BackendClan> getClan(String tag);

    Optional<BackendClan> getClan(UUID operator);

    void updateClan(BackendClan clan);
}
