package com.uroria.backend.common.stats;

import com.uroria.backend.common.helpers.PropertyHolder;
import com.uroria.backend.common.utils.ObjectUtils;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BackendStat extends PropertyHolder<BackendStat> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID uuid;
    private final int gameId;
    private final long time;
    private final Map<String, Long> scores;
    public BackendStat(@NonNull UUID uuid, int gameId, long time) {
        this.uuid = uuid;
        this.gameId = gameId;
        this.time = time;
        this.scores = new HashMap<>();
    }

    public Optional<Long> getScore(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(scores.get(key));
    }

    public void setScore(@NonNull String key, long score) {
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

    @Override
    public synchronized void modify(BackendStat stat) {
        ObjectUtils.overrideMap(scores, stat.scores);
        ObjectUtils.overrideMap(properties, stat.properties);
    }
}
