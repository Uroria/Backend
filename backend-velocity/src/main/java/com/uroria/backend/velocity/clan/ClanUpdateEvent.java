package com.uroria.backend.velocity.clan;

import com.uroria.backend.clan.Clan;
import lombok.Getter;

public final class ClanUpdateEvent {

    private @Getter final Clan clan;

    public ClanUpdateEvent(Clan clan) {
        this.clan = clan;
    }
}
