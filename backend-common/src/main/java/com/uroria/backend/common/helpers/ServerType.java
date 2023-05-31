package com.uroria.backend.common.helpers;

public enum ServerType {
    LOBBY(0),
    EVENT(1),
    GAME(2),
    OTHER(3);
    private final int id;

    ServerType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ServerType getById(int id) {
        for (ServerType serverType : values()) {
            if (serverType.getId() == id) return serverType;
        }
        return null;
    }
}
