package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ServerUpdate extends PulsarUpdate<Serverold> {
    private final BackendServerManager serverManager;

    public ServerUpdate(@NonNull PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:update", serverManager.getModuleName());
        this.serverManager = serverManager;
    }

    @Override
    protected void onUpdate(Serverold server) {
        this.serverManager.updateLocal(server);
    }
}
