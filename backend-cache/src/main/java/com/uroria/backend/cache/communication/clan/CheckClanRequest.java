package com.uroria.backend.cache.communication.clan;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class CheckClanRequest extends Request {
    private final String tag;
}
