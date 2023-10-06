package com.uroria.backend.server;

import com.uroria.annotations.markers.Warning;
import com.uroria.problemo.result.Result;

import java.util.Collection;
import java.util.Optional;

public interface ServerGroup extends ServerGroupTarget {

    Optional<Server> getServerByIdentifier(long identifier);

    Collection<Server> getServersWithTemplateId(int templateId);

    Collection<Server> getServers();

    @Warning(message = "Ordering a server could take more than 30 seconds. Use this method only if you know what you're doing.", suppress = "Okay, I understand")
    Result<Server> createServer(int templateId);
}
