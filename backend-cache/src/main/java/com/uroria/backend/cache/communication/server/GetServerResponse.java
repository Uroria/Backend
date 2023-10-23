package com.uroria.backend.cache.communication.server;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetServerResponse extends Response {
    private final boolean existent;
}
