package com.uroria.backend.cache.communication;

import com.uroria.backend.communication.broadcast.Broadcast;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class DeleteBroadcast extends Broadcast {
    private final String identifier;
}
