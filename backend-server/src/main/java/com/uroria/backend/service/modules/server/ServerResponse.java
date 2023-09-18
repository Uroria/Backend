package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.pulsarold.PulsarResponse;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ServerResponse extends PulsarResponse<Serverold, Long> {
    private final BackendServerManager serverManager;

    public ServerResponse(@NonNull PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:request:identifier", "server:response:identifier", serverManager.getModuleName());
        this.serverManager = serverManager;
    }

    @Override
    protected Serverold response(@NonNull Long key) {
        return this.serverManager.getServer(key, 0).orElse(null);
    }
}
