package com.uroria.backend.stats.events;

import com.uroria.backend.stats.Stat;
import lombok.NonNull;

public final class StatUpdatedEvent extends StatEvent {
    public StatUpdatedEvent(@NonNull Stat stat) {
        super(stat);
    }
}
