package com.uroria.backend.impl.ping;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@AllArgsConstructor
@Getter
public final class BackendPing implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final long identifier;
    private final long time;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BackendPing ping) {
            return ping.identifier == this.identifier;
        }
        return false;
    }
}
