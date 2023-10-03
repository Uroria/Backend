package com.uroria.backend.stats.events;

import com.uroria.backend.stats.Stat;
import lombok.NonNull;

public abstract class StatEvent {
    private final Stat stat;

    public StatEvent(@NonNull Stat stat) {
        this.stat = stat;
    }

    public Stat getStat() {
        return stat;
    }
}
