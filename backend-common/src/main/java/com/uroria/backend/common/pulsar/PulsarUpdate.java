package com.uroria.backend.common.pulsar;

import com.uroria.backend.common.utils.BackendInputStream;
import com.uroria.backend.common.utils.BackendOutputStream;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public abstract class PulsarUpdate<O> extends Thread {
    protected static final Logger LOGGER = LoggerFactory.getLogger("PulsarUpdate");

    private final PulsarClient pulsarClient;
    private final Producer<byte[]> producer;
    private final Consumer<byte[]> consumer;
    protected final String bridgeName;

    public PulsarUpdate(PulsarClient pulsarClient, String topic, String bridgeName) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.producer = pulsarClient.newProducer()
                .producerName(bridgeName)
                .topic(topic)
                .create();
        this.consumer = pulsarClient.newConsumer()
                .consumerName(bridgeName)
                .topic(topic)
                .subscriptionName(bridgeName)
                .negativeAckRedeliveryDelay(10, TimeUnit.MILLISECONDS)
                .subscribe();
        this.bridgeName = bridgeName;
        try {
            start();
        } catch (Exception exception) {
            LOGGER.error("Cannot start thread for update topic " + topic);
        }
    }

    protected abstract void onUpdate(O object);

    public final void update(O object) {
        if (object == null) throw new NullPointerException("Object cannot be null");
        try (BackendOutputStream output = new BackendOutputStream()) {
            output.writeObject(object);
            output.close();
            this.producer.send(output.toByteArray());
        } catch (Exception exception) {
            throw new RuntimeException("Cannot update object", exception);
        }
    }

    @Override
    public final void run() {
        String threadName = this.consumer.getTopic();
        Thread.currentThread().setName(threadName);
        LOGGER.info("Starting update thread for update topic " + threadName);
        try {
            while (!pulsarClient.isClosed()) {
                Message<byte[]> message = this.consumer.receive();
                if (message == null) continue;
                this.consumer.acknowledge(message);
                if (message.getProducerName().equals(this.bridgeName)) continue;
                CompletableFuture.runAsync(() -> {
                    try (BackendInputStream input = new BackendInputStream(message.getData())) {
                        onUpdate((O) input.readObject());
                    } catch (Exception exception) {
                        LOGGER.error("Cannot receive update of object", exception);
                    }
                });
            }
            close();
        } catch (PulsarClientException exception) {
            LOGGER.error("Error while trying to run listener thread", exception);
        }
    }

    public final void close() throws PulsarClientException {
        if (this.consumer.isConnected()) this.consumer.close();
        if (this.producer.isConnected()) this.producer.close();
    }
}
