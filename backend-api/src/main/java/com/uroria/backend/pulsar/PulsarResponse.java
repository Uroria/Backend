package com.uroria.backend.pulsar;

import com.uroria.backend.utils.BackendInputStream;
import com.uroria.backend.utils.BackendOutputStream;
import org.apache.pulsar.client.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

public abstract class PulsarResponse<K, O> extends Thread {
    protected static final Logger LOGGER = LoggerFactory.getLogger("PulsarResponse");

    private final PulsarClient pulsarClient;
    private final Producer<byte[]> producer;
    private final Consumer<byte[]> consumer;
    protected final String bridgeName;

    public PulsarResponse(PulsarClient pulsarClient, String requestTopic, String responseTopic, String bridgeName) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.producer = pulsarClient.newProducer()
                .producerName(bridgeName)
                .topic(responseTopic)
                .create();
        this.consumer = pulsarClient.newConsumer()
                .consumerName(bridgeName)
                .topic(requestTopic)
                .subscriptionName(bridgeName)
                .subscribe();
        this.bridgeName = bridgeName;
        try {
            start();
        } catch (Exception exception) {
            LOGGER.error("Cannot start thread for request topic " + requestTopic);
        }
    }

    protected abstract O response(K key);

    @Override
    public final void run() {
        String threadName = this.producer.getTopic();
        Thread.currentThread().setName(threadName);
        LOGGER.info("Starting response thread for response topic " + threadName);
        try {
            while (!this.pulsarClient.isClosed()) {
                Message<byte[]> message = this.consumer.receive();
                if (message == null) continue;
                this.consumer.acknowledge(message);
                CompletableFuture.runAsync(() -> {
                     try (BackendInputStream input = new BackendInputStream(message.getData())) {
                         K key = (K) input.readObject();
                         O obj = response(key);
                         BackendOutputStream output = new BackendOutputStream();
                         output.writeObject(key);
                         if (obj == null) {
                             output.writeBoolean(false);
                         } else {
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
        } catch (PulsarClientException exception) {
            LOGGER.error("Error while trying to run listener thread", exception);
        }
    }

    public final void close() throws PulsarClientException {
        if (this.consumer.isConnected()) this.consumer.close();
        if (this.producer.isConnected()) this.producer.close();
    }
}
