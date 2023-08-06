package com.uroria.backend.impl.server;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.server.Server;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.List;

public final class AllServersRequestChannel extends PulsarRequest<List<Server>, Integer> {
    public AllServersRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "server:request:all", "server:response:all", name, 5000);
    }
}
