package com.uroria.backend;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public abstract class AbstractManager {
    protected final PulsarClient pulsarClient;

    AbstractManager(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
    }

    abstract void start(String identifier) throws PulsarClientException;

    abstract void shutdown() throws PulsarClientException;
}
