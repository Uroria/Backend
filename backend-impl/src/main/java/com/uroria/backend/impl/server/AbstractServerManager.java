package com.uroria.backend.impl.server;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.server.BackendServer;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.utils.ObjectUtils;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;

public abstract class AbstractServerManager extends AbstractManager implements ServerManager {
    protected final Set<BackendServer> servers;

    public AbstractServerManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.servers = ObjectUtils.newSet();
    }

    abstract protected void checkServer(BackendServer server);

    @Override
    public Optional<BackendServer> getServer(int id) {
        return getServer(id, 5000);
    }
}
