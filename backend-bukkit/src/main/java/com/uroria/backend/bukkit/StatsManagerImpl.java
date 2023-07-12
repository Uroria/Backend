package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.events.StatUpdateEvent;
import com.uroria.backend.stats.BackendStat;
import com.uroria.backend.helpers.StatsRequest;
import com.uroria.backend.stats.BackendStatRequest;
import com.uroria.backend.stats.BackendStatUpdate;
import com.uroria.backend.stats.AbstractStatsManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class StatsManagerImpl extends AbstractStatsManager {

    private BackendStatRequest request;
    private BackendStatUpdate update;

    StatsManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
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
        CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new StatUpdateEvent(stat)));
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
    public Collection<BackendStat> getStats(@NotNull UUID holder, int gameId) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.RAW, null, 0, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreGreaterThanValue(@NotNull UUID holder, int gameId, @NotNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITH_SCORE_GREATER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreLowerThanValue(@NotNull UUID holder, int gameId, @NotNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITH_SCORE_LOWER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScore(@NotNull UUID holder, int gameId, @NotNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITH_SCORE, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsInTimeRangeOf(@NotNull UUID holder, int gameId, long startMs, long endMs) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(holder, gameId, StatsRequest.Action.WITHIN_TIME_RANGE, null, 0, startMs, endMs);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStats(int gameId) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.RAW, null, 0, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreGreaterThanValue(int gameId, @NotNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITH_SCORE_GREATER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScoreLowerThanValue(int gameId, @NotNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITH_SCORE_LOWER_THAN, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsWithScore(int gameId, @NotNull String scoreKey, long value) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITH_SCORE, scoreKey, value, 0, 0);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public Collection<BackendStat> getStatsInTimeRangeOf(int gameId, long startMs, long endMs) {
        Collection<BackendStat> stats = new ArrayList<>();

        if (BackendBukkitPlugin.isOffline()) {
            return stats;
        }

        StatsRequest statsRequest = new StatsRequest(null, gameId, StatsRequest.Action.WITHIN_TIME_RANGE, null, 0, startMs, endMs);

        Optional<Collection<BackendStat>> request = this.request.request(statsRequest);
        request.ifPresent(stats::addAll);
        return stats;
    }

    @Override
    public BackendStat updateStat(@NotNull BackendStat stat) {
        try {
            checkStat(stat);
            if (BackendBukkitPlugin.isOffline()) return stat;
            this.update.update(stat);
        } catch (Exception exception) {
            this.logger.error("Cannot update stat", exception);
            BackendAPIImpl.captureException(exception);
        }
        return stat;
    }
}
