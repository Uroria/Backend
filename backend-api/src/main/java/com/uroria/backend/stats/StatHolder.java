package com.uroria.backend.stats;

import lombok.NonNull;

public interface StatHolder extends Statistics {

    void addStat(int gameId, @NonNull String scoreKey, float value);

    void addStat(int gameId, @NonNull String scoreKey, int value);
}
