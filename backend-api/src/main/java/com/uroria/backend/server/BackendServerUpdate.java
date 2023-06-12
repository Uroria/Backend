package com.uroria.backend.server;

import com.uroria.backend.common.BackendServer;
import com.uroria.backend.common.pulsar.PulsarUpdate;
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
        LOGGER.info("Updating server with id " + object.getId().orElse(-1));
        serverConsumer.accept(object);
    }
}
