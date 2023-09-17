package com.uroria.backend.wrapper.stat;

import com.uroria.backend.impl.stats.AbstractStatsManager;
import com.uroria.backend.impl.stats.StatUpdateChannel;
import com.uroria.backend.impl.stats.StatsRequest;
import com.uroria.backend.impl.stats.StatsRequestChannel;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class StatsManagerImpl extends AbstractStatsManager implements StatsManager {
    private final boolean offline;
    private StatsRequestChannel request;
    private StatUpdateChannel update;

    public StatsManagerImpl(PulsarClient pulsarClient, Logger logger, boolean offline) {
        super(pulsarClient, logger);
        this.offline = offline;
    }

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.request = new StatsRequestChannel(this.pulsarClient, identifier);
        this.update = new StatUpdateChannel(this.pulsarClient, identifier, this::checkStat);
    }

    @Override
    protected void checkStat(StatOld stat) {
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.request != null) this.request.close();
        if (this.update != null) this.update.close();
    }

    @Override
    public Collection<StatOld> getStats(@NotNull UUID holder, int gameId) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.RAW, null, 0, 0, 0);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<StatOld> getStatsWithScoreGreaterThanValue(@NotNull UUID holder, int gameId, @NotNull String scoreKey, long value) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITH_SCORE_GREATER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<StatOld> getStatsWithScoreLowerThanValue(@NotNull UUID holder, int gameId, @NotNull String scoreKey, long value) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITH_SCORE_LOWER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<StatOld> getStatsWithScore(@NotNull UUID holder, int gameId, @NotNull String scoreKey, long value) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITH_SCORE, scoreKey, value, 0, 0);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<StatOld> getStatsInTimeRangeOf(@NotNull UUID holder, int gameId, long startMs, long endMs) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITHIN_TIME_RANGE, null, 0, startMs, endMs);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<StatOld> getStats(int gameId) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.RAW, null, 0, 0, 0);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<StatOld> getStatsWithScoreGreaterThanValue(int gameId, @NotNull String scoreKey, long value) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITH_SCORE_GREATER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<StatOld> getStatsWithScoreLowerThanValue(int gameId, @NotNull String scoreKey, long value) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITH_SCORE_LOWER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<StatOld> getStatsWithScore(int gameId, @NotNull String scoreKey, long value) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITH_SCORE, scoreKey, value, 0, 0);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<StatOld> getStatsInTimeRangeOf(int gameId, long startMs, long endMs) {
        Collection<StatOld> stats = new ArrayList<>();

        if (this.offline) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITHIN_TIME_RANGE, null, 0, startMs, endMs);

        Optional<Collection<StatOld>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public void updateStat(@NotNull StatOld stat) {
        try {
            checkStat(stat);
            if (this.offline) return;
            this.update.update(stat);
        } catch (Exception exception) {
            this.logger.error("Cannot update stat", exception);
        }
    }
}
