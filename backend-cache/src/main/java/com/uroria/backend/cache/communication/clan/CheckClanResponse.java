package com.uroria.backend.cache.communication.clan;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class CheckClanResponse extends Response {
    private final String tag;
    private final String name;
    private final boolean existent;
}
