package com.uroria.backend.impl.management;

import com.uroria.backend.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendRootStopUpdate extends PulsarUpdate<Boolean> {
    private final Runnable stopRunnable;
    public BackendRootStopUpdate(PulsarClient pulsarClient, String bridgeName, Runnable stopRunnable) throws PulsarClientException {
        super(pulsarClient, "backend:stop", bridgeName);
        this.stopRunnable = stopRunnable;
    }

    @Override
    protected void onUpdate(Boolean object) {
        stopRunnable.run();
    }
}
