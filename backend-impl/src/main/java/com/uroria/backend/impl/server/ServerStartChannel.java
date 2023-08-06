package com.uroria.backend.impl.server;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.server.Server;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ServerStartChannel extends PulsarRequest<Server, Server> {
    public ServerStartChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "server:start:request", "server:start:response", name, 300000);
    }
}
