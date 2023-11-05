package com.uroria.backend.cache.communication.server;

import com.uroria.backend.communication.broadcast.Broadcast;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class ServerPing extends Broadcast {
    private final long identifier;
    private final long currentMs;
    private final boolean disabled;
}
