package com.uroria.backend.impl;

import com.uroria.backend.Backend;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public abstract class AbstractBackend implements Backend {
    protected final PulsarClient pulsarClient;
    protected final String identifier;

    protected AbstractBackend(String pulsarURL) {
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

    protected AbstractBackend(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
        this.identifier = UUID.randomUUID().toString();
    }

    abstract public void start() throws PulsarClientException;

    public void shutdown() throws PulsarClientException {
        if (this.pulsarClient != null) this.pulsarClient.shutdown();
    }

    public final PulsarClient getPulsarClient() {
        return this.pulsarClient;
    }
}
