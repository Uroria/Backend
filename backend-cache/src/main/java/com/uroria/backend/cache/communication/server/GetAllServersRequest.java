package com.uroria.backend.cache.communication.server;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetAllServersRequest extends Request {
    private final boolean nothing;
}
