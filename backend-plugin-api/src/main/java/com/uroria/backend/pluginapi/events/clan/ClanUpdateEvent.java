package com.uroria.backend.pluginapi.events.clan;

import com.uroria.backend.clan.BackendClan;

public final class ClanUpdateEvent extends ClanEvent {
    public ClanUpdateEvent(BackendClan clan) {
        super(clan);
    }
}
