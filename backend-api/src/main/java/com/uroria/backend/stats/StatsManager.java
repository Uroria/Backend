package com.uroria.backend.stats;

import lombok.NonNull;

import java.util.Collection;
import java.util.UUID;

public interface StatsManager {

    Collection<Stat> getStats(@NonNull UUID holder, int gameId);

    Collection<Stat> getStatsWithScoreGreaterThanValue(@NonNull UUID uuid, int gameId, @NonNull String scoreKey, long value);

    Collection<Stat> getStatsWithScoreLowerThanValue(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value);

    Collection<Stat> getStatsWithScore(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value);

    Collection<Stat> getStatsInTimeRangeOf(@NonNull UUID holder, int gameId, long startMs, long endMs);

    Collection<Stat> getStats(int gameId);

    Collection<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, long value);

    Collection<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, long value);

    Collection<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, long value);

    Collection<Stat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs);

    void updateStat(@NonNull Stat stat);
}
