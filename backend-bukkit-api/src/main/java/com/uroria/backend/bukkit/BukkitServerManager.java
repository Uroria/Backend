package com.uroria.backend.bukkit;

import com.uroria.backend.common.BackendServer;
import com.uroria.backend.server.ServerManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class BukkitServerManager extends ServerManager {
    public BukkitServerManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
    }

    public abstract BackendServer getThisServer();
}
