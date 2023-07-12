package com.uroria.backend.stats;

import lombok.NonNull;

import java.util.Collection;
import java.util.UUID;

public interface StatsManager {

    Collection<BackendStat> getStats(@NonNull UUID holder, int gameId);

    Collection<BackendStat> getStatsWithScoreGreaterThanValue(@NonNull UUID uuid, int gameId, @NonNull String scoreKey, long value);

    Collection<BackendStat> getStatsWithScoreLowerThanValue(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value);

    Collection<BackendStat> getStatsWithScore(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value);

    Collection<BackendStat> getStatsInTimeRangeOf(@NonNull UUID holder, int gameId, long startMs, long endMs);

    Collection<BackendStat> getStats(int gameId);

    Collection<BackendStat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, long value);

    Collection<BackendStat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, long value);

    Collection<BackendStat> getStatsWithScore(int gameId, @NonNull String scoreKey, long value);

    Collection<BackendStat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs);

    BackendStat updateStat(@NonNull BackendStat stat);
}
