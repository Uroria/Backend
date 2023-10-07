package com.uroria.backend.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ServerStatus {
    EMPTY(0),
    STARTING(1),
    ONLINE(2),
    CLOSED(3),
    STOPPED(4);
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
