package com.uroria.backend.pluginapi.events.stats;

import com.uroria.backend.stats.BackendStat;

public final class StatUpdateEvent extends StatEvent {
    public StatUpdateEvent(BackendStat stat) {
        super(stat);
    }
}
