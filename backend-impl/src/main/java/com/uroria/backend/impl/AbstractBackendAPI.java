package com.uroria.backend.impl;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class AbstractBackendAPI {
    protected final PulsarClient pulsarClient;
    protected final String identifier;

    public AbstractBackendAPI(String pulsarURL) {
        if (pulsarURL == null) {
            this.pulsarClient = null;
            this.identifier = null;
            return;
        }
        try {
            this.pulsarClient = PulsarClient.builder()
                    .serviceUrl(pulsarURL)
                    .statsInterval(10, TimeUnit.MINUTES)
                    .build();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        this.identifier = UUID.randomUUID().toString();
    }

    abstract protected void start() throws PulsarClientException;

    protected void shutdown() throws PulsarClientException {
        if (this.pulsarClient == null) return;
        this.pulsarClient.shutdown();
    }
}