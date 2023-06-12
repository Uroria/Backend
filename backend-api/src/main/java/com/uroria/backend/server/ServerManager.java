package com.uroria.backend.server;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.BackendServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class ServerManager extends AbstractManager {
    protected final Logger logger;
    protected final Collection<BackendServer> servers;

    public ServerManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.servers = new ArrayList<>();
    }

    abstract protected void checkServer(BackendServer server);

    abstract public Optional<BackendServer> getServer(int id, int timeout);

    abstract public List<Integer> getAllServers(int timeout);

    abstract public void updateServer(BackendServer server);

    abstract public BackendServer startServer(BackendServer server);
}
