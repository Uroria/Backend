package com.uroria.backend.impl.pulsar;

import com.uroria.backend.utils.BackendInputStream;
import com.uroria.backend.utils.BackendOutputStream;
import lombok.NonNull;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class PulsarResponse<O, K> extends Thread {
    protected static final Logger LOGGER = Pulsar.getLogger();

    private final PulsarClient pulsarClient;
    private final Producer<byte[]> producer;
    private final Consumer<byte[]> consumer;
    protected final String name;

    public PulsarResponse(@NonNull PulsarClient pulsarClient, @NonNull String requestTopic, @NonNull String responseTopic, @NonNull String name) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.producer = pulsarClient.newProducer()
                .producerName(name)
                .topic(responseTopic)
                .create();
        this.consumer = pulsarClient.newConsumer()
                .consumerName(name)
                .subscriptionName(name)
                .topic(requestTopic)
                .negativeAckRedeliveryDelay(5, TimeUnit.MILLISECONDS)
                .ackTimeout(30, TimeUnit.MINUTES)
                .subscribe();
        this.name = name;
        try {
            start();
        } catch (Exception exception) {
            LOGGER.error("Cannot start thread for request topic " + requestTopic, exception);
            throw new PulsarClientException(exception);
        }
    }

    protected abstract O response(@NonNull K key);

    @Override
    public final void run() {
        String threadName = "Response=" + this.consumer.getTopic();
        Thread.currentThread().setName(threadName);
        LOGGER.debug("Starting thread " + threadName);
        try {
            while (!this.pulsarClient.isClosed()) {
                Message<byte[]> message = this.consumer.receive();
                if (message == null) continue;
                this.consumer.acknowledge(message);
                CompletableFuture.runAsync(() -> {
                    try (BackendInputStream input = new BackendInputStream(message.getData())) {
                        K key = (K) input.readObject();
                        O obj = response(key);
                        input.close();
                        BackendOutputStream output = new BackendOutputStream();
                        output.writeObject(key);
                        if (obj == null) output.writeBoolean(false);
                        else {
                            output.writeBoolean(true);
                            output.writeObject(obj);
                        }
                        output.close();
                        this.producer.send(output.toByteArray());
                    } catch (Exception exception) {
                        LOGGER.error("Cannot receive request of object", exception);
                    }
                });
            }
        } catch (Exception exception) {
            LOGGER.error("Unhandled exception in PulsarResponse", exception);
        }
    }

    public final void close() throws PulsarClientException {
        if (this.consumer.isConnected()) this.consumer.close();
        if (this.producer.isConnected()) this.producer.close();
    }
}
