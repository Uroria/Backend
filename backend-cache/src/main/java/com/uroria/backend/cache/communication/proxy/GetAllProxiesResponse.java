package com.uroria.backend.cache.communication.proxy;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
@Getter
public final class GetAllProxiesResponse extends Response {
    private final Collection<Long> proxies;
}
