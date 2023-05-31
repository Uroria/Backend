package com.uroria.backend.api.events.stats;

import com.uroria.backend.common.BackendStat;

public final class StatRegisterEvent extends StatEvent {
    public StatRegisterEvent(BackendStat stat) {
        super(stat);
    }
}
