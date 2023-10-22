package com.uroria.backend.cache.communication;

import com.google.gson.JsonElement;
import com.uroria.backend.communication.broadcast.Broadcast;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class UpdateBroadcast extends Broadcast {
    private final String identifier;
    private final String key;
    private final JsonElement element;
}
