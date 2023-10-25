package com.uroria.backend.cache.communication.permgroup;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
@Getter
public final class GetAllGroupResponse extends Response {
    private final Collection<String> groups;
}
