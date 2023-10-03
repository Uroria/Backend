package com.uroria.backend.stats.events;

import com.uroria.backend.stats.Stat;
import lombok.NonNull;

public final class StatDeletedEvent extends StatEvent {
    public StatDeletedEvent(@NonNull Stat stat) {
        super(stat);
    }
}
