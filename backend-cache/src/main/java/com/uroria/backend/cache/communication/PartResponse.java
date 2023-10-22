package com.uroria.backend.cache.communication;

import com.google.gson.JsonElement;
import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class PartResponse extends Response {
    private final String identifier;
    private final String key;
    private final JsonElement value;
}
