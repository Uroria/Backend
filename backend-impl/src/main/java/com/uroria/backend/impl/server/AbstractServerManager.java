package com.uroria.backend.impl.server;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractServerManager extends AbstractManager implements ServerManager {
    protected final ObjectArraySet<Server> servers;

    public AbstractServerManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.servers = new ObjectArraySet<>();
    }

    abstract protected void checkServer(@NonNull Server server);
}
