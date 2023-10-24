package com.uroria.backend.cache.communication.server.group;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetServerGroupResponse extends Response {
    private final boolean existent;
}
