package com.uroria.backend.cache.communication.proxy;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetProxyRequest extends Request {
    private final long id;
    private final boolean autoCreate;
}
