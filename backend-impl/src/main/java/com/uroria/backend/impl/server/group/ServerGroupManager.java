package com.uroria.backend.impl.server.group;

import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.impl.server.ServerManager;
import com.uroria.backend.server.events.ServerGroupDeletedEvent;
import com.uroria.backend.server.events.ServerGroupUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerGroupManager extends WrapperManager<ServerGroupWrapper> {
    private static final Logger logger = LoggerFactory.getLogger("ServerGroups");

    private final ServerManager serverManager;

    public ServerGroupManager(ServerManager serverManager, Communicator communicator) {
        super(logger, communicator, "server_group", "server_group", "server_group");
        this.serverManager = serverManager;
    }

    @Override
    protected void onUpdate(ServerGroupWrapper wrapper) {
        if (wrapper.isDeleted()) eventManager.callAndForget(new ServerGroupDeletedEvent(wrapper));
        else eventManager.callAndForget(new ServerGroupUpdatedEvent(wrapper));
    }
}
