package com.uroria.backend.cache.communication.server;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
@Getter
public final class GetAllServersResponse extends Response {
    private final Collection<Long> servers;
}
