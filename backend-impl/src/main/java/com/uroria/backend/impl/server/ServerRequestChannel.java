package com.uroria.backend.impl.server;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.server.Server;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ServerRequestChannel extends PulsarRequest<Server, Long> {

    public ServerRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "server:request:identifier", "server:response:identifier", name, 4000);
    }
}
