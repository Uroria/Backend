package com.uroria.backend.pluginapi.modules;

import com.uroria.backend.common.BackendStat;

import java.util.Collection;
import java.util.UUID;

public interface StatsManager {
    Collection<BackendStat> getStats(UUID holder, int gameId);

    Collection<BackendStat> getStatsWithScoreGreaterThanValue(UUID holder, int gameId, String scoreKey, long value);

    Collection<BackendStat> getStatsWithScoreLowerThanValue(UUID holder, int gameId, String scoreKey, long value);

    Collection<BackendStat> getStatsWithScore(UUID holder, int gameId, String scoreKey, long value);

    Collection<BackendStat> getStatsInTimeRangeOf(UUID holder, int gameId, long startMs, long endMs);

    Collection<BackendStat> getStats(int gameId);

    Collection<BackendStat> getStatsWithScoreGreaterThanValue(int gameId, String scoreKey, long value);

    Collection<BackendStat> getStatsWithScoreLowerThanValue(int gameId, String scoreKey, long value);

    Collection<BackendStat> getStatsWithScore(int gameId, String scoreKey, long value);

    Collection<BackendStat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs);

    void updateStat(BackendStat stat);
}
