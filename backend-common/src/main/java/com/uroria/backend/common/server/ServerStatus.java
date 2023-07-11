package com.uroria.backend.common.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ServerStatus {
    EMPTY(0),
    STARTING(1),
    READY(2),
    INGAME(3),
    ENDING(4),
    CLOSED(5),
    STOPPED(6);
    private @Getter final int id;

    public static ServerStatus getById(int id) {
        for (ServerStatus serverStatus : values()) {
            if (serverStatus.getId() == id) return serverStatus;
        }
        return EMPTY;
    }
}
