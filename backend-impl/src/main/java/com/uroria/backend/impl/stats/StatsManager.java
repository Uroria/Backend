package com.uroria.backend.impl.stats;

import com.rabbitmq.client.Connection;
import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.stats.Stat;
import com.uroria.backend.stats.Statistics;
import lombok.NonNull;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public final class StatsManager extends AbstractManager implements Statistics {
    public StatsManager(Connection rabbit) {
        super(rabbit, LoggerFactory.getLogger("Statistics"));
    }

    @Override
    protected void start() throws Exception {

    }

    @Override
    protected void shutdown() throws Exception {

    }

    public void addStat(UUID uuid, int gameId, String scoreKey, float value) {

    }

    public void addStat(UUID uuid, int gameId, String scoreKey, int value) {

    }

    public List<Stat> getStatsWithScoreGreaterThanValue(UUID uuid, int gameId, @NonNull String scoreKey, int value) {
        return null;
    }

    public List<Stat> getStatsWithScoreLowerThanValue(UUID uuid, int gameId, @NonNull String scoreKey, int value) {
        return null;
    }

    public List<Stat> getStatsWithScore(UUID uuid, int gameId, @NonNull String scoreKey, int value) {
        return null;
    }

    public List<Stat> getStatsWithScoreGreaterThanValue(UUID uuid, int gameId, @NonNull String scoreKey, float value) {
        return null;
    }

    public List<Stat> getStatsWithScoreLowerThanValue(UUID uuid, int gameId, @NonNull String scoreKey, float value) {
        return null;
    }

    public List<Stat> getStatsWithScore(UUID uuid, int gameId, @NonNull String scoreKey, float value) {
        return null;
    }

    public List<Stat> getStats(UUID uuid, int gameId) {
        return null;
    }

    public List<Stat> getStatsInTimeRangeOf(UUID uuid, int gameId, long startMs, long endMs) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, int value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, int value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, int value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, float value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, float value) {
        return null;
    }

    @Override
    public List<Stat> getStatsWithScore(int gameId, @NonNull String scoreKey, float value) {
        return null;
    }

    @Override
    public List<Stat> getStats(int gameId) {
        return null;
    }

    @Override
    public List<Stat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs) {
        return null;
    }
}
