package com.uroria.backend.velocity.events;

import com.uroria.backend.common.BackendStat;

public final class StatUpdateEvent {
    private final BackendStat stat;

    public StatUpdateEvent(BackendStat stat) {
        this.stat = stat;
    }

    public BackendStat getStat() {
        return stat;
    }
}
