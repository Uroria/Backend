package com.uroria.backend.pluginapi.modules;

import com.uroria.backend.common.BackendParty;

import java.util.Optional;
import java.util.UUID;

public interface PartyManager {
    Optional<BackendParty> getParty(UUID operator);
    void updateParty(BackendParty party);
}
