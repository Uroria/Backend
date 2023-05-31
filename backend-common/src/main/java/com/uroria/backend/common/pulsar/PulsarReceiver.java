package com.uroria.backend.common.pulsar;

import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public abstract class PulsarReceiver extends Thread {
    private static final Logger LOGGER = LoggerFactory.getLogger(PulsarReceiver.class);

    private final PulsarClient pulsarClient;
    private final Consumer<byte[]> consumer;
    protected final String receiverName;
    private final int timeout;
    public PulsarReceiver(PulsarClient pulsarClient, String topic, String receiverName, int timeout) throws PulsarClientException {
        this.pulsarClient = pulsarClient;
        this.consumer = pulsarClient.newConsumer()
                .consumerName(receiverName)
                .topic(topic)
                .subscriptionName(receiverName)
                .negativeAckRedeliveryDelay(timeout, TimeUnit.MILLISECONDS)
                .subscribe();
        this.receiverName = receiverName;
        this.timeout = timeout;
        start();
    }

    @Override
    public void run() {
        try {
            while (!pulsarClient.isClosed()) {
                Message<byte[]> message = this.consumer.receive(timeout, TimeUnit.MILLISECONDS);
                if (message == null) continue;
                onReceive(this.consumer, message);
            }
            close();
        } catch (PulsarClientException exception) {
            LOGGER.error("Error while trying to run listener thread", exception);
        }
    }

    public abstract void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException;

    public final void close() throws PulsarClientException {
        if (this.consumer.isConnected()) this.consumer.close();
    }
}
