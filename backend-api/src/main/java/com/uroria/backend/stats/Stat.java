package com.uroria.backend.stats;

import com.uroria.backend.Deletable;
import com.uroria.backend.user.User;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.NonNull;

import java.util.Optional;

public interface Stat extends Deletable {

    User getUser();

    int getGameId();

    long getDate();

    void setScore(@NonNull String key, int score);

    default Optional<Integer> getScore(String key) {
        int score = getScoreOrElse(key, -1);
        if (score == -1) return Optional.empty();
        return Optional.of(score);
    }

    int getScoreOrElse(String key, int defValue);

    Object2IntMap<String> getScores();
}
