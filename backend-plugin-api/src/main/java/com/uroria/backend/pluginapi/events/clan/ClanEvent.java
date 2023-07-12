package com.uroria.backend.pluginapi.events.clan;

import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.pluginapi.events.Event;

public abstract class ClanEvent extends Event {
    private final BackendClan clan;

    public ClanEvent(BackendClan clan) {
        this.clan = clan;
    }

    public BackendClan getClan() {
        return clan;
    }
}
