package com.uroria.backend.velocity.events;

import com.uroria.backend.stats.BackendStat;

public final class StatUpdateEvent {
    private final BackendStat stat;

    public StatUpdateEvent(BackendStat stat) {
        this.stat = stat;
    }

    public BackendStat getStat() {
        return stat;
    }
}
