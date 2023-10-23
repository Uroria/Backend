package com.uroria.backend.cache.communication.proxy;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetProxyResponse extends Response {
    private final boolean existent;
}
