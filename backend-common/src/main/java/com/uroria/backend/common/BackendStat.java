package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BackendStat extends PropertyHolder implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID uuid;
    private final int gameId;
    private final long time;
    private final Map<String, Long> scores;
    public BackendStat(UUID uuid, int gameId, long time) {
        this.uuid = uuid;
        this.gameId = gameId;
        this.time = time;
        this.scores = new HashMap<>();
    }

    public Optional<Long> getScore(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(scores.get(key));
    }

    public void setScore(String key, long score) {
        if (key == null) throw new NullPointerException("Key cannot be null");
        this.scores.put(key, score);
    }

    public Map<String, Long> getScores() {
        return new HashMap<>(this.scores);
    }

    public long getTime() {
        return time;
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getGameId() {
        return gameId;
    }
}
