package com.uroria.backend.impl.player;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.player.BackendPlayer;
import com.uroria.backend.player.PlayerManager;
import com.uroria.backend.utils.ObjectUtils;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractPlayerManager extends AbstractManager implements PlayerManager {
    protected final Set<BackendPlayer> players;
    public AbstractPlayerManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.players = ObjectUtils.newSet();
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
