package com.uroria.backend.impl.server;

import com.uroria.backend.pulsar.PulsarRequest;
import com.uroria.backend.server.BackendServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendServerRequest extends PulsarRequest<BackendServer, Integer> {
    public BackendServerRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "server:request:id", "server:response:id", bridgeName, 5000, 10);
    }
}
