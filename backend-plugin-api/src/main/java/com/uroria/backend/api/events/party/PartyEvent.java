package com.uroria.backend.api.events.party;

import com.uroria.backend.api.events.Event;
import com.uroria.backend.common.BackendParty;

public abstract class PartyEvent extends Event {
    private final BackendParty party;

    public PartyEvent(BackendParty party) {
        this.party = party;
    }

    public BackendParty getParty() {
        return party;
    }
}
