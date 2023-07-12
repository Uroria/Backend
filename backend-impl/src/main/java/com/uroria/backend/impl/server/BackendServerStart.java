package com.uroria.backend.impl.server;

import com.uroria.backend.pulsar.PulsarRequest;
import com.uroria.backend.server.BackendServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendServerStart extends PulsarRequest<BackendServer, BackendServer> {
    public BackendServerStart(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "server:start:request", "server:start:acknowledge", bridgeName, 1000000, 20);
    }
}
