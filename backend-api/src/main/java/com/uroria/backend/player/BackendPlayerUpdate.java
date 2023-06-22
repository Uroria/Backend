package com.uroria.backend.player;

import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class BackendPlayerUpdate extends PulsarUpdate<BackendPlayer> {
    private final Consumer<BackendPlayer> playerConsumer;

    public BackendPlayerUpdate(PulsarClient pulsarClient, String bridgeName, Consumer<BackendPlayer> playerConsumer) throws PulsarClientException {
        super(pulsarClient, "player:update", bridgeName);
        this.playerConsumer = playerConsumer;
    }

    @Override
    public void onUpdate(BackendPlayer object) {
        playerConsumer.accept(object);
    }
}
