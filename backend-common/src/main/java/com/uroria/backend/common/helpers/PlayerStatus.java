package com.uroria.backend.common.helpers;

public enum PlayerStatus {
    OFFLINE(0),
    ONLINE(1),
    DO_NOT_DISTURB(2),
    LIVE(3);
    private final int id;
    PlayerStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static PlayerStatus getById(int id) {
        for (PlayerStatus status : values()) {
            if (status.getId() == id) return status;
        }
        return null;
    }
}
