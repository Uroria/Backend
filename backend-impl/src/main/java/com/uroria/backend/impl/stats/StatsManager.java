package com.uroria.backend.impl.stats;

import com.uroria.backend.impl.BackendWrapperImpl;
import com.uroria.backend.stats.Stat;
import com.uroria.backend.stats.Statistics;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import lombok.NonNull;

import java.util.List;

public final class StatsManager implements Statistics {

    public StatsManager(BackendWrapperImpl wrapper) {

    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, int value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, int value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, int value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, float value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, float value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, float value) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStats(int gameId) {
        return ObjectLists.emptyList();
    }

    @Override
    public List<Stat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs) {
        return ObjectLists.emptyList();
    }
}
