package com.uroria.backend.pluginapi.events.clan;

import com.uroria.backend.clan.BackendClan;

public final class ClanCreateEvent extends ClanEvent {
    public ClanCreateEvent(BackendClan clan) {
        super(clan);
    }
}
