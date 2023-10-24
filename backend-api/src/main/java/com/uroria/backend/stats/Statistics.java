package com.uroria.backend.stats;

import com.uroria.annotations.safety.TimeConsuming;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

public interface Statistics {

    @ApiStatus.Experimental
    @TimeConsuming
    List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, int value);

    @ApiStatus.Experimental
    @TimeConsuming
    List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, int value);

    @ApiStatus.Experimental
    @TimeConsuming
    List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, int value);

    @ApiStatus.Experimental
    @TimeConsuming
    List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, float value);

    @ApiStatus.Experimental
    @TimeConsuming
    List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, float value);

    @ApiStatus.Experimental
    @TimeConsuming
    List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, float value);

    @ApiStatus.Experimental
    @TimeConsuming
    List<Stat> getStats(int gameId);

    @ApiStatus.Experimental
    @TimeConsuming
    List<Stat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs);
}
