package com.uroria.backend.cache.communication.user;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetUserResponse extends Response {
    private final boolean existent;
    private final UUID uuid;
}
