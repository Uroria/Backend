package com.uroria.backend.cache.communication.server.group;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetAllServersGroupsRequest extends Request {
    private final boolean nothing; // lol
}
