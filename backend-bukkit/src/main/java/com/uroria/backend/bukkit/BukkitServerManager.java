package com.uroria.backend.bukkit;

import com.uroria.backend.impl.server.AbstractServerManager;
import com.uroria.backend.server.BackendServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class BukkitServerManager extends AbstractServerManager {
    public BukkitServerManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
    }

    public abstract BackendServer getThisServer();
}
