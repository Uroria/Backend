package com.uroria.backend.player;

import com.uroria.backend.AbstractManager;
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

    abstract protected void checkPlayer(BackendPlayer player);

    /**
     * Get a player from Backend. If the player is not registered it will be created and registered.
     * @param timeout Request timeout in ms
     * @throws NullPointerException If UUID is null
     */
    abstract public Optional<BackendPlayer> getPlayer(UUID uuid, int timeout);

    /**
     * @param timeout Request timeout in ms
     * @throws NullPointerException If name is null
     */
    abstract public Optional<BackendPlayer> getPlayer(String name, int timeout);

    /**
     * Update a player and broadcast it to all servers. If the player is not registered, nothing should happen.
     * @throws NullPointerException If player is null
     */
    abstract public void updatePlayer(BackendPlayer player);
}
