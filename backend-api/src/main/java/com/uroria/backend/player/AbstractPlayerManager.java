package com.uroria.backend.player;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.player.BackendPlayer;
import com.uroria.backend.common.player.PlayerManager;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractPlayerManager extends AbstractManager implements PlayerManager {
    protected final Logger logger;
    protected final Collection<BackendPlayer> players;
    public AbstractPlayerManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.players = new CopyOnWriteArrayList<>();
    }

    abstract protected void checkPlayer(BackendPlayer player);

    @Override
    public Optional<BackendPlayer> getPlayer(@NonNull UUID uuid) {
        return getPlayer(uuid, 3000);
    }

    @Override
    public Optional<BackendPlayer> getPlayer(@NonNull String name) {
        return getPlayer(name, 3000);
    }
}
