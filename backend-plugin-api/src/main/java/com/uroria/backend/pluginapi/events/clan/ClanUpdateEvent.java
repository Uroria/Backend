package com.uroria.backend.pluginapi.events.clan;

import com.uroria.backend.common.clan.BackendClan;

public final class ClanUpdateEvent extends ClanEvent {
    public ClanUpdateEvent(BackendClan clan) {
        super(clan);
    }
}
