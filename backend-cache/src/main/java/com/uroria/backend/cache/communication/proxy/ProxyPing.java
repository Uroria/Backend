package com.uroria.backend.cache.communication.proxy;

import com.uroria.backend.communication.broadcast.Broadcast;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public final class ProxyPing extends Broadcast {
    private final long identifier;
    private final long currentMs;
    private final boolean disabled;
}
