package com.uroria.backend.clan.events;

import com.uroria.backend.clan.Clan;
import lombok.NonNull;

public final class ClanDeletedEvent extends ClanEvent {

    public ClanDeletedEvent(@NonNull Clan clan) {
        super(clan);
    }
}
