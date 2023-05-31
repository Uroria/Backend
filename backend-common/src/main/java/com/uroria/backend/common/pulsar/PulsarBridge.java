package com.uroria.backend.common.pulsar;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class PulsarBridge extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(PulsarBridge.class);

    private final PulsarClient pulsarClient;
    private final Producer<byte[]> producer;
    private final Consumer<byte[]> consumer;
    protected final String bridgeName;
    private final int timeout;
    public PulsarBridge(PulsarClient pulsarClient, String topic, String bridgeName, int timeout) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.producer = pulsarClient.newProducer()
                .producerName(bridgeName)
                .topic(topic)
                .create();
        this.consumer = pulsarClient.newConsumer()
                .consumerName(bridgeName)
                .topic(topic)
                .subscriptionName(bridgeName)
                .negativeAckRedeliveryDelay(timeout, TimeUnit.MILLISECONDS)
                .subscribe();
        this.bridgeName = bridgeName;
        this.timeout = timeout;
        start();
    }

    @Override
    public final void run() {
        try {
            while (!pulsarClient.isClosed()) {
                Message<byte[]> message = this.consumer.receive(this.timeout, TimeUnit.MILLISECONDS);
                if (message == null) continue;
                onReceive(this.consumer, message);
            }
            close();
        } catch (PulsarClientException exception) {
            LOGGER.error("Error while trying to run listener thread", exception);
        }
    }

    public abstract void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException;

    public final void send(byte[] message) throws PulsarClientException {
        this.producer.send(message);
    }

    public final void close() throws PulsarClientException {
        if (this.consumer.isConnected()) this.consumer.close();
        if (this.producer.isConnected()) this.producer.close();
    }
}
