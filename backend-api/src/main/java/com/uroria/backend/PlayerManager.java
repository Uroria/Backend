package com.uroria.backend;

import com.uroria.backend.common.BackendPlayer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class PlayerManager extends AbstractManager {
    protected final Logger logger;
    protected final Collection<BackendPlayer> players;
    public PlayerManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.players = new CopyOnWriteArrayList<>();
    }

    @Override
    abstract protected void start(String identifier);

    @Override
    abstract protected void shutdown();

    abstract public Optional<BackendPlayer> getPlayer(UUID uuid);

    abstract public Optional<BackendPlayer> getPlayer(String name);

    abstract public void updatePlayer(BackendPlayer player);
}
