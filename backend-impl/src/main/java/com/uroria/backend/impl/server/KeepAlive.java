package com.uroria.backend.impl.server;

import com.uroria.backend.impl.pulsar.PulsarKeepAlive;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class KeepAlive extends PulsarKeepAlive {
    public KeepAlive(PulsarClient pulsarClient, String bridgeName, long identifier) throws PulsarClientException {
        super(pulsarClient, "backend:keepalive", bridgeName, identifier);
    }
}
