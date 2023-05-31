package com.uroria.backend.api.events.party;

import com.uroria.backend.common.BackendParty;

public final class PartyDeleteEvent extends PartyEvent {
    public PartyDeleteEvent(BackendParty party) {
        super(party);
    }
}
