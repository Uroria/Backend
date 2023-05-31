package com.uroria.backend.common.helpers;

public enum ServerStatus {
    EMPTY(0),
    PREPARED(1),
    STARTING(2),
    ONLINE(3),
    OFFLINE(4),
    DELETED(5);
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
        return null;
    }
}
