package com.uroria.backend.stats;

import com.uroria.backend.Deletable;
import com.uroria.backend.user.User;
import lombok.NonNull;

import java.util.Map;
import java.util.Optional;

public interface Stat extends Deletable {

    User getUser();

    int getGameId();

    long getDate();

    void setScore(@NonNull String key, int score);

    void setScore(@NonNull String key, float score);

    default Optional<Integer> getScoreInt(String key) {
        int score = getScoreOrElse(key, -1);
        if (score == -1) return Optional.empty();
        return Optional.of(score);
    }

    default Optional<Float> getScoreFloat(String key) {
        float score = getScoreOrElse(key, -1f);
        if (score == -1) return Optional.empty();
        return Optional.of(score);
    }

    int getScoreOrElse(String key, int defValue);

    float getScoreOrElse(String key, float defValue);

    Map<String, Number> getScores();
}
