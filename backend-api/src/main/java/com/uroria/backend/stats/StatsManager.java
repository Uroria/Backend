package com.uroria.backend.stats;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.BackendStat;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.UUID;

public abstract class StatsManager extends AbstractManager {
    protected final Logger logger;

    public StatsManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
    }

    protected abstract void checkStat(BackendStat stat);

    public abstract void updateStat(BackendStat stat);

    public abstract Collection<BackendStat> getStats(UUID holder, int gameId);

    public abstract Collection<BackendStat> getStatsWithScoreGreaterThanValue(UUID holder, int gameId, String scoreKey, long value);

    public abstract Collection<BackendStat> getStatsWithScoreLowerThanValue(UUID holder, int gameId, String scoreKey, long value);

    public abstract Collection<BackendStat> getStatsWithScore(UUID holder, int gameId, String scoreKey, long value);

    public abstract Collection<BackendStat> getStatsInTimeRangeOf(UUID holder, int gameId, long startMs, long endMs);

    public abstract Collection<BackendStat> getStats(int gameId);

    public abstract Collection<BackendStat> getStatsWithScoreGreaterThanValue(int gameId, String scoreKey, long value);

    public abstract Collection<BackendStat> getStatsWithScoreLowerThanValue(int gameId, String scoreKey, long value);

    public abstract Collection<BackendStat> getStatsWithScore(int gameId, String scoreKey, long value);

    public abstract Collection<BackendStat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs);
}
