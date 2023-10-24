package com.uroria.backend.cache.communication.server.group;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetServerGroupRequest extends Request {
    private final String name;
    private final boolean autoCreate;
}
