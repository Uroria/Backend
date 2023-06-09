package com.uroria.backend.common.helpers;

public enum ServerStatus {
    EMPTY(0),
    STARTING(1),
    READY(2),
    INGAME(3),
    CLOSED(4),
    STOPPED(5);
    private final int id;

    ServerStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ServerStatus getById(int id) {
        for (ServerStatus serverStatus : values()) {
            if (serverStatus.getId() == id) return serverStatus;
        }
        return EMPTY;
    }
}
