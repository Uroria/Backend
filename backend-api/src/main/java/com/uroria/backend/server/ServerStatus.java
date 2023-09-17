package com.uroria.backend.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ServerStatus {
    EMPTY(0),
    STARTING(1),
    QUEUE(2),
    INGAME(3),
    CLOSED(4),
    STOPPED(6);
    private final int id;

    public int getID() {
        return this.id;
    }

    public static ServerStatus getById(int id) {
        for (ServerStatus serverStatus : values()) {
            if (serverStatus.getID() == id) return serverStatus;
        }
        return EMPTY;
    }
}
