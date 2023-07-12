package com.uroria.backend.pluginapi.events.clan;

import com.uroria.backend.clan.BackendClan;

public final class ClanDeleteEvent extends ClanEvent {
    public ClanDeleteEvent(BackendClan clan) {
        super(clan);
    }
}
