package com.uroria.backend.server.modules.server;

import com.uroria.backend.server.BackendServer;
import com.uroria.backend.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendServerResponse extends PulsarResponse<Integer, BackendServer> {
    private final BackendServerManager serverManager;

    public BackendServerResponse(PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:request:id", "server:response:id", "ServerModule");
        this.serverManager = serverManager;
    }

    @Override
    protected BackendServer response(Integer key) {
        return this.serverManager.getServer(key).orElse(null);
    }
}
