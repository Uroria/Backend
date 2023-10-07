package com.uroria.backend.app;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ApplicationStatus {
    EMPTY(0),
    STARTING(1),
    ONLINE(2),
    CLOSED(3),
    STOPPED(4);
    private final int id;

    public int getID() {
        return this.id;
    }

    public static ApplicationStatus getById(int id) {
        for (ApplicationStatus status : values()) {
            if (status.id == id) return status;
        }
        return EMPTY;
    }
}
