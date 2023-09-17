package com.uroria.backend.server;

import java.util.Collection;
import java.util.Optional;

public interface ServerGroup extends ServerGroupTarget {

    Optional<Server> getServerByIdentifier(long identifier);

    Collection<Server> getServersWithTemplateId(int templateId);
}
