package com.uroria.backend.cache.communication.clan;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetClanResponse extends Response {
    private final boolean existent;
}
