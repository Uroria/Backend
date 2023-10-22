package com.uroria.backend.cache.communication;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class PartRequest extends Request {
    private final String identifier;
    private final String key;
}
