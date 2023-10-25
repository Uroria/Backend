package com.uroria.backend.cache.communication.proxy;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Getter
public final class GetAllProxiesRequest extends Request {
    @Nullable
    private final String name;
}
