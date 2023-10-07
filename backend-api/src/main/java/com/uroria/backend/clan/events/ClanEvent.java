package com.uroria.backend.clan.events;

import com.uroria.backend.clan.Clan;
import lombok.NonNull;

public abstract class ClanEvent {
    private final Clan clan;

    public ClanEvent(@NonNull Clan clan) {
        this.clan = clan;
    }

    public Clan getClan() {
        return clan;
    }
}
