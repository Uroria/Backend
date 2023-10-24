package com.uroria.backend.cache.communication.server.group;

import com.uroria.backend.communication.response.Response;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;

@AllArgsConstructor
@Getter
public final class GetAllServersGroupsResponse extends Response {
    private final Collection<String> groups;
}
