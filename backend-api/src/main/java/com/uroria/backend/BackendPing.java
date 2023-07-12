package com.uroria.backend;

import java.io.Serial;
import java.io.Serializable;

public final class BackendPing implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final long identifier;
    private final long time;

    public BackendPing(long identifier, long time) {
        this.identifier = identifier;
        this.time = time;
    }

    public long getIdentifier() {
        return identifier;
    }

    public long getTime() {
        return time;
    }
}
