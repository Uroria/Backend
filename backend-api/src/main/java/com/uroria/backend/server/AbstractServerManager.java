package com.uroria.backend.server;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.server.BackendServer;
import com.uroria.backend.common.server.ServerManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractServerManager extends AbstractManager implements ServerManager {
    protected final Logger logger;
    protected final Collection<BackendServer> servers;

    public AbstractServerManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.servers = new CopyOnWriteArrayList<>();
    }

    abstract protected void checkServer(BackendServer server);

    @Override
    public Optional<BackendServer> getServer(int id) {
        return getServer(id, 5000);
    }
}
