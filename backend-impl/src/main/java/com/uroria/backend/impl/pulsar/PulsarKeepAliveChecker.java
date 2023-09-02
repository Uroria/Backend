package com.uroria.backend.impl.pulsar;

import com.uroria.backend.impl.ping.BackendPing;
import com.uroria.base.io.InsaneByteArrayInputStream;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class PulsarKeepAliveChecker extends Thread {
    private static final Logger LOGGER = Pulsar.getLogger();

    private final PulsarClient pulsarClient;
    private final Consumer<byte[]> consumer;
    protected final String bridgeName;
    protected final ObjectArraySet<BackendPing> keepAlives;

    public PulsarKeepAliveChecker(PulsarClient pulsarClient, String topic, String bridgeName) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.consumer = pulsarClient.newConsumer()
                .consumerName(bridgeName)
                .topic(topic)
                .subscriptionName(bridgeName)
                .negativeAckRedeliveryDelay(10, TimeUnit.MILLISECONDS)
                .ackTimeout(20000, TimeUnit.MILLISECONDS)
                .subscribe();
        this.bridgeName = bridgeName;
        this.keepAlives = new ObjectArraySet<>();
        start();
    }

    protected abstract void onTimeout(BackendPing ping);

    private void checkKeepAlives() {
        for (BackendPing ping : this.keepAlives) {
            if ((System.currentTimeMillis() - ping.getTime()) < 10000) continue;
            CompletableFuture.runAsync(() -> onTimeout(ping));
        }
    }

    @Override
    public void run() {
        while (!this.pulsarClient.isClosed() && this.consumer.isConnected()) {
            try {
                CompletableFuture.runAsync(this::checkKeepAlives);
                Message<byte[]> message = this.consumer.receive(20, TimeUnit.MILLISECONDS);
                if (message == null) continue;
                consumer.acknowledge(message);
                CompletableFuture.runAsync(() -> {
                    try (InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(message.getData())) {
                        BackendPing ping = (BackendPing) input.readObject();
                        input.close();
                        this.keepAlives.removeIf(ping::equals);
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

    public final void close() throws PulsarClientException {
        if (!this.consumer.isConnected()) this.consumer.close();
    }
}
