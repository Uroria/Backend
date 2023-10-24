package com.uroria.backend.cache.communication.user;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public final class GetUserRequest extends Request {
    private final UUID uuid;
    private final String name;
    private final boolean autoCreate;
}
