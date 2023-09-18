package com.uroria.backend.impl;

import com.uroria.backend.BackendWrapper;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.concurrent.TimeUnit;

public abstract class AbstractBackendWrapper implements BackendWrapper {
    protected final PulsarClient pulsarClient;

    protected AbstractBackendWrapper(String pulsarURL) {
        if (pulsarURL == null) {
            this.pulsarClient = null;
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
    }

    protected AbstractBackendWrapper(PulsarClient pulsarClient) {
        this.pulsarClient = pulsarClient;
    }

    abstract public void start() throws PulsarClientException;

    public void shutdown() throws PulsarClientException {
        if (this.pulsarClient != null) this.pulsarClient.shutdown();
    }

    public final PulsarClient getPulsarClient() {
        return this.pulsarClient;
    }
}
