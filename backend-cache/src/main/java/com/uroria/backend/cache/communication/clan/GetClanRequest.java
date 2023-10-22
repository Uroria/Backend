package com.uroria.backend.cache.communication.clan;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetClanRequest extends Request {
    private final String name;
}
