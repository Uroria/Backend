package com.uroria.backend.impl;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

public abstract class AbstractManager {
    protected final PulsarClient pulsarClient;
    protected final Logger logger;

    public AbstractManager(PulsarClient pulsarClient, Logger logger) {
        this.pulsarClient = pulsarClient;
        this.logger = logger;
    }

    abstract protected void start(String identifier) throws PulsarClientException;

    abstract protected void shutdown() throws PulsarClientException;
}
