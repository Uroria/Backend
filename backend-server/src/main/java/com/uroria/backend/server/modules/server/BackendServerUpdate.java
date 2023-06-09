package com.uroria.backend.server.modules.server;

import com.uroria.backend.common.BackendServer;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendServerUpdate extends PulsarUpdate<BackendServer> {
    private final BackendServerManager serverManager;

    public BackendServerUpdate(PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:update", "ServerModule");
        this.serverManager = serverManager;
    }

    @Override
    protected void onUpdate(BackendServer object) {
        this.serverManager.updateLocal(object);
    }
}
