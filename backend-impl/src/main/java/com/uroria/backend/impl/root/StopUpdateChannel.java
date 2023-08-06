package com.uroria.backend.impl.root;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class StopUpdateChannel extends PulsarUpdate<Boolean> {
    private final Runnable stopRunnable;

    public StopUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Runnable stopRunnable) throws PulsarClientException {
        super(pulsarClient, "backend:stopall", name);
        this.stopRunnable = stopRunnable;
    }

    @Override
    protected void onUpdate(Boolean object) {
        this.stopRunnable.run();
    }
}
