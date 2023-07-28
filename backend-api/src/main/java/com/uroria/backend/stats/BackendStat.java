package com.uroria.backend.stats;

import com.uroria.backend.helpers.PropertyHolder;
import com.uroria.backend.utils.ObjectUtils;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class BackendStat extends PropertyHolder<BackendStat> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID uuid;
    private final int gameId;
    private final long time;
    private final Object2IntMap<String> scores;

    public BackendStat(@NonNull UUID uuid, int gameId, long time) {
        this.uuid = uuid;
        this.gameId = gameId;
        this.time = time;
        this.scores = new Object2IntArrayMap<>();
    }

    public Optional<Integer> getScore(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(scores.get(key));
    }

    public void setScore(@NonNull String key, int score) {
        this.scores.put(key, score);
    }

    public Map<String, Integer> getScores() {
        return Object2IntMaps.unmodifiable(this.scores);
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
