package com.uroria.backend.cache.communication.controls;

import com.uroria.backend.communication.broadcast.Broadcast;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class PingUpdate extends Broadcast {
    private final String appId;
    private final long currentMs;
}
