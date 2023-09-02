package com.uroria.backend.impl.pulsar;

import com.uroria.base.io.InsaneByteArrayInputStream;
import com.uroria.base.io.InsaneByteArrayOutputStream;
import lombok.NonNull;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.io.Serializable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class PulsarUpdate<O extends Serializable> extends Thread {
    protected static final Logger LOGGER = Pulsar.getLogger();

    private final PulsarClient pulsarClient;
    private final Producer<byte[]> producer;
    private final Consumer<byte[]> consumer;
    protected final String name;

    public PulsarUpdate(@NonNull PulsarClient pulsarClient, @NonNull String topic, @NonNull String name) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.producer = pulsarClient.newProducer()
                .producerName(name)
                .topic(topic)
                .create();
        this.consumer = pulsarClient.newConsumer()
                .topic(topic)
                .consumerName(name)
                .subscriptionName(name)
                .negativeAckRedeliveryDelay(5, TimeUnit.MILLISECONDS)
                .ackTimeout(30, TimeUnit.MINUTES)
                .subscribe();
        this.name = name;
        try {
            start();
        } catch (Exception exception) {
            LOGGER.error("Cannot start thread for update topic " + topic, exception);
            throw new PulsarClientException(exception);
        }
    }

    protected abstract void onUpdate(O object);

    public final void update(@NonNull O object) {
        try (InsaneByteArrayOutputStream output = new InsaneByteArrayOutputStream()) {
            output.writeObject(object);
            output.close();
            this.producer.send(output.toByteArray());
        } catch (Exception exception) {
            LOGGER.error("Cannot update object " + object, exception);
            throw new RuntimeException(exception);
        }
    }

    @Override
    public final void run() {
        String threadName = "Update=" + this.consumer.getTopic();
        Thread.currentThread().setName(threadName);
        LOGGER.debug("Starting thread " + threadName);
        try {
            while (!this.pulsarClient.isClosed()) {
                Message<byte[]> message = this.consumer.receive();
                if (message == null) continue;
                this.consumer.acknowledge(message);
                if (message.getProducerName().equals(this.name)) continue;
                CompletableFuture.runAsync(() -> {
                    try (InsaneByteArrayInputStream input = new InsaneByteArrayInputStream(message.getData())) {
                        onUpdate((O) input.readObject());
                    } catch (Exception exception) {
                        LOGGER.error("Unhandled exception in ObjectInputStream in PulsarUpdate", exception);
                    }
                });
            }
        } catch (Exception exception) {
            LOGGER.error("Unhandled exception in PulsarUpdate thread", exception);
        }
    }

    public final void close() throws PulsarClientException {
        if (this.consumer.isConnected()) this.consumer.close();
        if (this.producer.isConnected()) this.producer.close();
    }
}
