package com.uroria.backend.server;

import com.uroria.backend.common.BackendServer;
import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendServerStart extends PulsarRequest<BackendServer, BackendServer> {
    public BackendServerStart(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "server:start:request", "server:start:acknowledge", bridgeName, 5000, 20);
    }
}