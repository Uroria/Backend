package com.uroria.backend.cache.communication.permgroup;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetGroupResponse extends Response {
    private final boolean existent;
}
