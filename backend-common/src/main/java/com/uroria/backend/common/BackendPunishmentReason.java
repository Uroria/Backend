package com.uroria.backend.common;

import java.io.Serial;
import java.io.Serializable;

public final class BackendPunishmentReason implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final int id;
    private final String display;
    public BackendPunishmentReason(int id, String display) {
        this.id = id;
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public int getId() {
        return id;
    }
}
