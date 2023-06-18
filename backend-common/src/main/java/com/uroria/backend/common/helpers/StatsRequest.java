package com.uroria.backend.common.helpers;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public final class StatsRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1;
    private final UUID holder;
    private final int gameId;
    private final Action action;

    private final String scoreKey;
    private final long scoreValue;

    private final long startMs;
    private final long endMs;

    public StatsRequest(UUID holder, int gameId, Action action, String scoreKey, long scoreValue, long startMs, long endMs) {
        this.holder = holder;
        this.gameId = gameId;
        this.action = action;
        this.scoreKey = scoreKey;
        this.scoreValue = scoreValue;
        this.startMs = startMs;
        this.endMs = endMs;
    }

    public String getScoreKey() {
        return scoreKey;
    }

    public long getScoreValue() {
        return scoreValue;
    }

    public long getStartMs() {
        return startMs;
    }

    public long getEndMs() {
        return endMs;
    }

    public Optional<UUID> getHolder() {
        return Optional.ofNullable(holder);
    }

    public int getGameId() {
        return gameId;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {
        RAW,
        WITH_SCORE_GREATER_THAN,
        WITH_SCORE_LOWER_THAN,
        WITH_SCORE,
        WITHIN_TIME_RANGE;
    }
}
