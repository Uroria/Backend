package com.uroria.backend.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ServerType {
    OTHER(0),
    LOBBY(1),
    GAME(2),
    EVENT(3),
    CUSTOM_EVENT(4);
    private final int id;

    public int getID() {
        return this.id;
    }

    public static ServerType getById(int id) {
        for (ServerType serverType : values()) {
            if (serverType.getID() == id) return serverType;
        }
        return OTHER;
    }
}
