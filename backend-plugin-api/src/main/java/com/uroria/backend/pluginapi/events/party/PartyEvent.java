package com.uroria.backend.pluginapi.events.party;

import com.uroria.backend.pluginapi.events.Event;
import com.uroria.backend.party.BackendParty;

public abstract class PartyEvent extends Event {
    private final BackendParty party;

    public PartyEvent(BackendParty party) {
        this.party = party;
    }

    public BackendParty getParty() {
        return party;
    }
}
