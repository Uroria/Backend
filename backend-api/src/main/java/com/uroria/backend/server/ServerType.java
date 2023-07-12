package com.uroria.backend.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ServerType {
    EMPTY(0),
    EVENT(1),
    LOBBY(2),
    GAME(3),
    CUSTOM_EVENT(4),
    OTHER(5);
    private @Getter final int id;

    public static ServerType getById(int id) {
        for (ServerType serverType : values()) {
            if (serverType.getId() == id) return serverType;
        }
        return OTHER;
    }
}
