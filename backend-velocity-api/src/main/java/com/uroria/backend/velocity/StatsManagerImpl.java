package com.uroria.backend.velocity;

import com.uroria.backend.common.helpers.StatsRequest;
import com.uroria.backend.stats.BackendStatRequest;
import com.uroria.backend.stats.BackendStatUpdate;
import com.uroria.backend.stats.AbstractStatsManager;
import com.uroria.backend.common.stats.BackendStat;
import com.uroria.backend.velocity.events.StatUpdateEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public final class StatsManagerImpl extends AbstractStatsManager {
    private final ProxyServer proxyServer;

    private BackendStatRequest request;
    private BackendStatUpdate update;

    StatsManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    protected void start(String identifier) {
        try {
            this.request = new BackendStatRequest(this.pulsarClient, identifier);
            this.update = new BackendStatUpdate(this.pulsarClient, identifier, this::checkStat);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void checkStat(BackendStat stat) {
        this.proxyServer.getEventManager().fireAndForget(new StatUpdateEvent(stat));
    }

    @Override
    protected void shutdown() {
        try {
            if (this.request != null) this.request.close();
            if (this.update != null) this.update.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    public Collection<BackendStat> getStats(@NonNull UUID holder, int gameId) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.RAW, null, 0, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreGreaterThanValue(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITH_SCORE_GREATER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreLowerThanValue(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITH_SCORE_LOWER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScore(@NonNull UUID holder, int gameId, @NonNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITH_SCORE, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsInTimeRangeOf(@NonNull UUID holder, int gameId, long startMs, long endMs) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITHIN_TIME_RANGE, null, 0, startMs, endMs);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStats(int gameId) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.RAW, null, 0, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreGreaterThanValue(int gameId, @NonNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITH_SCORE_GREATER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreLowerThanValue(int gameId, @NonNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITH_SCORE_LOWER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScore(int gameId, @NonNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITH_SCORE, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs) {
        Collection<BackendStat> stats = new ArrayList<>();

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITHIN_TIME_RANGE, null, 0, startMs, endMs);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest, 3000);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public BackendStat updateStat(@NonNull BackendStat stat) {
        try {
            checkStat(stat);
            this.update.update(stat);
        } catch (Exception exception) {
            this.logger.error("Cannot update stat", exception);
            BackendAPIImpl.captureException(exception);
        }
        return stat;
    }
}
