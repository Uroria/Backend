package com.uroria.backend.party;

import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

public interface PartyManager {

    Optional<BackendParty> getParty(@NonNull UUID operator, int timeout);

    Optional<BackendParty> getParty(@NonNull UUID operator);

    BackendParty updateParty(@NonNull BackendParty party);
}
