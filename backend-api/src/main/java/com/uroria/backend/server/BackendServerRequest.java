package com.uroria.backend.server;

import com.uroria.backend.common.BackendServer;
import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendServerRequest extends PulsarRequest<BackendServer, Integer> {
    public BackendServerRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "server:request:id", "server:response:id", bridgeName, 5000, 10);
    }
}
