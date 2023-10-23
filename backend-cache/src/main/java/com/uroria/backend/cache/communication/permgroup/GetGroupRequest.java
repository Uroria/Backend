package com.uroria.backend.cache.communication.permgroup;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetGroupRequest extends Request {
    private final String name;
    private final boolean autoCreate;
}
