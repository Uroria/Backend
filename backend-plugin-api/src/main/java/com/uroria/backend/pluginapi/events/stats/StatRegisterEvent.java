package com.uroria.backend.pluginapi.events.stats;

import com.uroria.backend.common.BackendStat;

public final class StatRegisterEvent extends StatEvent {
    public StatRegisterEvent(BackendStat stat) {
        super(stat);
    }
}
