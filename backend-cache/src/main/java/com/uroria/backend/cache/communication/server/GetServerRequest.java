package com.uroria.backend.cache.communication.server;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetServerRequest extends Request {
    private final long id;
    private final boolean autoCreate;
}
