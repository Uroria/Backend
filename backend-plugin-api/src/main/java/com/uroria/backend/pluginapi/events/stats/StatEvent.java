package com.uroria.backend.pluginapi.events.stats;

import com.uroria.backend.pluginapi.events.Event;
import com.uroria.backend.stats.BackendStat;

public abstract class StatEvent extends Event {
    private final BackendStat stat;

    public StatEvent(BackendStat stat) {
        this.stat = stat;
    }

    public BackendStat getStat() {
        return stat;
    }
}
