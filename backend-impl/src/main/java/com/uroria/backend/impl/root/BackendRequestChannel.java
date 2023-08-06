package com.uroria.backend.impl.root;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendRequestChannel extends PulsarRequest<Boolean, Long> {
    public BackendRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "backend:online:request", "backend:online:response", name, 10000);
    }
}
