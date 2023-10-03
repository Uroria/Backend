package com.uroria.backend.stats;

import com.uroria.annotations.safety.TimeConsuming;
import lombok.NonNull;

import java.util.List;

public interface StatHolder {

    void addStat(int gameId, @NonNull String scoreKey, float value);

    void addStat(int gameId, @NonNull String scoreKey, int value);

    @TimeConsuming
    List<Stat> getStats(int gameId);

    @TimeConsuming
    List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, int value);

    @TimeConsuming
    List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, int value);

    @TimeConsuming
    List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, int value);

    @TimeConsuming
    List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, float value);

    @TimeConsuming
    List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, float value);

    @TimeConsuming
    List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, float value);

    @TimeConsuming
    List<Stat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs);
}
