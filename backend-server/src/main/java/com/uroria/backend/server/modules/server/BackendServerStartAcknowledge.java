package com.uroria.backend.server.modules.server;

import com.uroria.backend.common.server.BackendServer;
import com.uroria.backend.common.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendServerStartAcknowledge extends PulsarResponse<BackendServer, BackendServer> {
    private final BackendServerManager serverManager;

    public BackendServerStartAcknowledge(PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:start:request", "server:start:acknowledge", "ServerModule");
        this.serverManager = serverManager;
    }

    @Override
    protected BackendServer response(BackendServer key) {
        return this.serverManager.startServer(key);
    }
}
