package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import com.uroria.backend.server.Server;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ServerStart extends PulsarResponse<Server, Server> {
    private final BackendServerManager serverManager;

    public ServerStart(@NonNull PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:start:request", "server:start:response", serverManager.getModuleName());
        this.serverManager = serverManager;
    }

    @Override
    protected Server response(@NonNull Server server) {
        return this.serverManager.startServer(server);
    }
}
