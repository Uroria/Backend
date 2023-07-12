package com.uroria.backend.impl.management;

import com.uroria.backend.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendRootBackendRequest extends PulsarRequest<Boolean, Integer> {
    public BackendRootBackendRequest(PulsarClient pulsarClient, String bridgeName, int timeout) throws PulsarClientException {
        super(pulsarClient, "backend:online:request", "backend:online:response", bridgeName, timeout, 5);
    }
}
