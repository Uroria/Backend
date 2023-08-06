package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import com.uroria.backend.server.Server;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ServerResponse extends PulsarResponse<Server, Long> {
    private final BackendServerManager serverManager;

    public ServerResponse(@NonNull PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:request", "server:response", serverManager.getModuleName());
        this.serverManager = serverManager;
    }

    @Override
    protected Server response(@NonNull Long key) {
        return this.serverManager.getServer(key, 0).orElse(null);
    }
}
