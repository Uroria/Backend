package com.uroria.backend.common.pulsar;

import org.apache.pulsar.client.api.Producer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public abstract class PulsarSender {
    private final Producer<byte[]> producer;
    protected final String senderName;
    public PulsarSender(PulsarClient pulsarClient, String topic, String senderName) throws PulsarClientException {
        this.producer = pulsarClient.newProducer().producerName(senderName).topic(topic).create();
        this.senderName = senderName;
    }

    public final void send(byte[] message) throws PulsarClientException {
        this.producer.send(message);
    }

    public final void close() throws PulsarClientException {
        if (this.producer.isConnected()) this.producer.close();
    }
}
