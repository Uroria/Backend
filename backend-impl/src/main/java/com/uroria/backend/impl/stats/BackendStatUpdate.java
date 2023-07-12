package com.uroria.backend.impl.stats;

import com.uroria.backend.pulsar.PulsarUpdate;
import com.uroria.backend.stats.BackendStat;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class BackendStatUpdate extends PulsarUpdate<BackendStat> {
    private final Consumer<BackendStat> statConsumer;

    public BackendStatUpdate(PulsarClient pulsarClient, String bridgeName, Consumer<BackendStat> statConsumer) throws PulsarClientException {
        super(pulsarClient, "stat:update", bridgeName);
        this.statConsumer = statConsumer;
    }

    @Override
    protected void onUpdate(BackendStat object) {
        LOGGER.info("Updating stat with game-id " + object.getGameId());
        statConsumer.accept(object);
    }
}
