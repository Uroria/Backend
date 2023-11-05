package com.uroria.backend.cache.communication.controls;

import com.uroria.backend.communication.broadcast.Broadcast;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class ShutdownBroadcast extends Broadcast {
    private final boolean general;
}
