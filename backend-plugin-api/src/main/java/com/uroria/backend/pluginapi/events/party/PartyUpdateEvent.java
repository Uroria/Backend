package com.uroria.backend.pluginapi.events.party;

import com.uroria.backend.common.party.BackendParty;

public final class PartyUpdateEvent extends PartyEvent {
    public PartyUpdateEvent(BackendParty party) {
        super(party);
    }
}
