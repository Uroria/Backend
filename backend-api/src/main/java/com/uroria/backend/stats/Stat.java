package com.uroria.backend.stats;

import com.uroria.backend.Backend;
import com.uroria.backend.BackendObject;
import com.uroria.base.utils.CollectionUtils;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class Stat extends BackendObject<Stat> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private final int gameId;
    private @Getter final long time;
    private final Object2IntMap<String> scores;

    public Stat(@NonNull UUID uuid, int gameId, long time) {
        this.uuid = uuid;
        this.gameId = gameId;
        this.time = time;
        this.scores = new Object2IntArrayMap<>();
    }

    public Optional<Integer> getScore(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(this.scores.get(key));
    }

    public void setScore(@NonNull String key, int score) {
        this.scores.put(key, score);
    }

    public Map<String, Integer> getScores() {
        return Collections.unmodifiableMap(this.scores);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public int getGameID() {
        return this.gameId;
    }

    @Override
    public void modify(Stat stat) {
        CollectionUtils.overrideMap(this.scores, stat.scores);
    }

    @Override
    public void update() {
        Backend.getAPI().getStatsManager().updateStat(this);
    }
}
