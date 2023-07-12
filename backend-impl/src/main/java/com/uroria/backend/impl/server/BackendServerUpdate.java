package com.uroria.backend.impl.server;

import com.uroria.backend.pulsar.PulsarUpdate;
import com.uroria.backend.server.BackendServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class BackendServerUpdate extends PulsarUpdate<BackendServer> {
    private final Consumer<BackendServer> serverConsumer;
    public BackendServerUpdate(PulsarClient pulsarClient, String bridgeName, Consumer<BackendServer> serverConsumer) throws PulsarClientException {
        super(pulsarClient, "server:update", bridgeName);
        this.serverConsumer = serverConsumer;
    }

    @Override
    public void onUpdate(BackendServer object) {
        serverConsumer.accept(object);
    }
}
