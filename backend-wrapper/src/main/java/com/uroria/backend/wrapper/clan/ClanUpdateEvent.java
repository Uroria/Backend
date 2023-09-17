package com.uroria.backend.wrapper.clan;

import lombok.Getter;

public final class ClanUpdateEvent {

    private @Getter final ClanOld clan;

    public ClanUpdateEvent(ClanOld clan) {
        this.clan = clan;
    }
}
