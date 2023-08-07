package com.uroria.backend.velocity.punishment;

import com.uroria.backend.punishment.Punished;
import lombok.Getter;

public final class PunishedUpdateEvent {

    private @Getter final Punished punished;

    public PunishedUpdateEvent(Punished punished) {
        this.punished = punished;
    }
}
