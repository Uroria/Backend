package com.uroria.backend.api.events.stats;

import com.uroria.backend.api.events.Event;
import com.uroria.backend.common.BackendStat;

public abstract class StatEvent extends Event {
    private final BackendStat stat;

    public StatEvent(BackendStat stat) {
        this.stat = stat;
    }

    public BackendStat getStat() {
        return stat;
    }
}
