package com.uroria.backend.common.pulsar;

import com.uroria.backend.common.BackendPing;
import com.uroria.backend.common.utils.BackendInputStream;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public abstract class PulsarKeepAliveChecker extends Thread {
    protected static final Logger LOGGER = LoggerFactory.getLogger("PulsarKeepAlive");

    private final PulsarClient pulsarClient;
    private final Consumer<byte[]> consumer;
    protected final String bridgeName;
    protected final Collection<BackendPing> keepAlives;

    public PulsarKeepAliveChecker(PulsarClient pulsarClient, String topic, String bridgeName) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.consumer = pulsarClient.newConsumer()
                .consumerName(bridgeName)
                .subscriptionName(bridgeName)
                .topic(topic)
                .negativeAckRedeliveryDelay(10, TimeUnit.MILLISECONDS)
                .ackTimeout(20000, TimeUnit.MILLISECONDS)
                .subscribe();
        this.bridgeName = bridgeName;
        this.keepAlives = new CopyOnWriteArrayList<>();
    }

    protected abstract void onTimeout(BackendPing obj);

    private void checkKeepAlives() {
        for (BackendPing entry : this.keepAlives) {
            if ((System.currentTimeMillis() - entry.getTime()) < 10000) continue;
            CompletableFuture.runAsync(() -> {
                onTimeout(entry);
            });
        }
    }

    public final void close() throws PulsarClientException {
        this.consumer.close();
    }

    @Override
    public final void run() {
        while (!this.pulsarClient.isClosed() && this.consumer.isConnected()) {
            try {
                CompletableFuture.runAsync(this::checkKeepAlives);
                Message<byte[]> message = this.consumer.receive(20, TimeUnit.MILLISECONDS);
                if (message == null) continue;
                consumer.acknowledge(message);
                CompletableFuture.runAsync(() -> {
                    try (BackendInputStream input = new BackendInputStream(message.getData())) {
                        BackendPing ping = (BackendPing) input.readObject();
                        input.close();
                        this.keepAlives.removeIf(ping1 -> ping1.getIdentifier() == ping.getIdentifier());
                        this.keepAlives.add(ping);
                    } catch (Exception exception) {
                        LOGGER.error("Cannot read keep alive message", exception);
                    }
                });
            } catch (Exception exception) {
                LOGGER.warn("Could not receive keep alives", exception);
            }
        }
    }
}
