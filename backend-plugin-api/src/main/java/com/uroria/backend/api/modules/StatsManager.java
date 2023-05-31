package com.uroria.backend.api.modules;

import com.uroria.backend.common.BackendStat;

import java.util.Optional;
import java.util.UUID;

public interface StatsManager {
    Optional<BackendStat> getStat(UUID holder);
    void updateStat(BackendStat stat);
}
