package com.uroria.backend.impl;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public abstract class AbstractManager {
    protected final PulsarClient pulsarClient;

    public AbstractManager(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
    }

    abstract protected void start(String identifier);

    abstract protected void shutdown();
}
