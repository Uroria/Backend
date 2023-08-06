package com.uroria.backend.impl.server;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.server.Server;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class ServerUpdateChannel extends PulsarUpdate<Server> {
    private final Consumer<Server> serverConsumer;

    public ServerUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<Server> serverConsumer) throws PulsarClientException {
        super(pulsarClient, "server:update", name);
        this.serverConsumer = serverConsumer;
    }

    @Override
    protected void onUpdate(Server server) {
        this.serverConsumer.accept(server);
    }
}
