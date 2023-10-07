package com.uroria.backend.user.punishment;

public interface PermPunishment extends Punishment {

    @Override
    default boolean isPermanent() {
        return true;
    }
}
