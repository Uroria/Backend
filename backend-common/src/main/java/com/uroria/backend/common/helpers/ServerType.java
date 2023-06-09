package com.uroria.backend.common.helpers;

public enum ServerType {
    EMPTY(0),
    EVENT(1),
    LOBBY(2),
    GAME(3),
    CUSTOM_EVENT(4),
    OTHER(5);
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
        return OTHER;
    }
}
