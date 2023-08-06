package com.uroria.backend.user;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum OnlineStatus {
    INVISIBLE(0),
    ONLINE(1),
    DO_NOT_DISTURB(2),
    LIVE(3);

    private final int id;

    public int getID() {
        return this.id;
    }

    public static OnlineStatus byID(int id) {
        return Arrays.stream(values()).filter(s -> s.id == id).findAny().orElse(null);
    }
}
