package com.uroria.backend.user.punishment;

public interface TempPunishment extends Punishment {

    long getEndDate();

    @Override
    default boolean isPermanent() {
        return false;
    }
}
