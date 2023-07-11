package com.uroria.backend.server.modules.stats;

import com.uroria.backend.common.stats.BackendStat;
import com.uroria.backend.common.helpers.StatsRequest;
import com.uroria.backend.common.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.ArrayList;
import java.util.Collection;

public final class BackendStatsResponse extends PulsarResponse<StatsRequest, Collection<BackendStat>> {
    private final BackendStatsManager statsManager;
    public BackendStatsResponse(PulsarClient pulsarClient, BackendStatsManager statsManager) throws PulsarClientException {
        super(pulsarClient, "stat:request", "stat:response", "StatsModule");
        this.statsManager = statsManager;
    }

    @Override
    protected Collection<BackendStat> response(StatsRequest request) {
        Collection<BackendStat> stats = new ArrayList<>();
        switch (request.getAction()) {
            case RAW -> {
                if (request.getHolder().isPresent()) stats.addAll(this.statsManager.getStats(request.getHolder().get(), request.getGameId()));
                else stats.addAll(this.statsManager.getStats(request.getGameId()));
            }
            case WITH_SCORE -> {
                if (request.getHolder().isPresent()) stats.addAll(this.statsManager.getStatsWithScore(request.getHolder().get(), request.getGameId(), request.getScoreKey(), request.getScoreValue()));
                else stats.addAll(this.statsManager.getStatsWithScore(request.getGameId(), request.getScoreKey(), request.getScoreValue()));
            }
            case WITHIN_TIME_RANGE -> {
                if (request.getHolder().isPresent()) stats.addAll(this.statsManager.getStatsInTimeRangeOf(request.getHolder().get(), request.getGameId(), request.getStartMs(), request.getEndMs()));
                else stats.addAll(this.statsManager.getStatsInTimeRangeOf(request.getGameId(), request.getStartMs(), request.getEndMs()));
            }
            case WITH_SCORE_LOWER_THAN -> {
                if (request.getHolder().isPresent()) stats.addAll(this.statsManager.getStatsWithScoreLowerThanValue(request.getHolder().get(), request.getGameId(), request.getScoreKey(), request.getScoreValue()));
                else stats.addAll(this.statsManager.getStatsWithScoreLowerThanValue(request.getGameId(), request.getScoreKey(), request.getScoreValue()));
            }
            case WITH_SCORE_GREATER_THAN -> {
                if (request.getHolder().isPresent()) stats.addAll(this.statsManager.getStatsWithScoreGreaterThanValue(request.getHolder().get(), request.getGameId(), request.getScoreKey(), request.getScoreValue()));
                else stats.addAll(this.statsManager.getStatsWithScoreGreaterThanValue(request.getGameId(), request.getScoreKey(), request.getScoreValue()));
            }
        };
        return stats;
    }
}
