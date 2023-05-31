package com.uroria.backend.api.events.party;

import com.uroria.backend.common.BackendParty;

public final class PartyUpdateEvent extends PartyEvent {
    public PartyUpdateEvent(BackendParty party) {
        super(party);
    }
}
