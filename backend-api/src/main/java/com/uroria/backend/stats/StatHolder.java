package com.uroria.backend.stats;

import lombok.NonNull;

import java.util.List;
import java.util.UUID;

public interface StatHolder {

    List<Stat> getStats(@NonNull UUID holder, int gameId);

    List<Stat> getStatsWithScoreGreaterThanValue(@NonNull UUID uuid, int gameId, @NonNull String scoreKey, long value);

    List<Stat> getStatsWithScoreLowerThanValue(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value);

    List<Stat> getStatsWithScore(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value);

    List<Stat> getStatsInTimeRangeOf(@NonNull UUID holder, int gameId, long startMs, long endMs);

    List<Stat> getStats(int gameId);

    List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, long value);

    List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, long value);

    List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, long value);

    List<Stat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs);
}
