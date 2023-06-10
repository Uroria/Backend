package com.uroria.backend.pluginapi.events.clan;

import com.uroria.backend.common.BackendClan;

public final class ClanCreateEvent extends ClanEvent {
    public ClanCreateEvent(BackendClan clan) {
        super(clan);
    }
}
