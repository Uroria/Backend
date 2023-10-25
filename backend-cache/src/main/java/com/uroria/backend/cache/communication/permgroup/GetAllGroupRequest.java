package com.uroria.backend.cache.communication.permgroup;

import com.uroria.backend.communication.request.Request;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public final class GetAllGroupRequest extends Request {
    private final boolean nothing; // Didn't expect that, didn't  you?
}
