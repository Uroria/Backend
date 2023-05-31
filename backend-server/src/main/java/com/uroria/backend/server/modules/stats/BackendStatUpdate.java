package com.uroria.backend.server.modules.stats;

import com.uroria.backend.common.BackendStat;
import com.uroria.backend.common.pulsar.PulsarBridge;
import com.uroria.backend.server.Uroria;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.CompletableFuture;

public final class BackendStatUpdate extends PulsarBridge {
    private final Logger logger;
    private final BackendStatsManager statsManager;
    BackendStatUpdate(Logger logger, BackendStatsManager statsManager, PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "stats:update", "StatsModule", 100000);
        this.logger = logger;
        this.statsManager = statsManager;
    }

    @Override
    public void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException {
        consumer.acknowledge(message);
        if (message.getProducerName().equals(this.bridgeName)) return;
        CompletableFuture.runAsync(() -> {
            BackendStat stat;
            try {
                ByteArrayInputStream inputBuffer = new ByteArrayInputStream(message.getData());
                ObjectInputStream input = new ObjectInputStream(inputBuffer);
                stat = (BackendStat) input.readObject();
                input.close();
                inputBuffer.close();
            } catch (Exception exception) {
                this.logger.error("Cannot update stat by " + message.getProducerName(), exception);
                Uroria.captureException(exception);
                return;
            }
            this.statsManager.updateStat(stat);
        });
    }
}
