package com.uroria.backend.impl.server;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.server.Server;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class ServerIDRequestChannel extends PulsarRequest<Server, Integer> {

    public ServerIDRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "server:request:id", "server:response:id", name, 4000);
    }
}
