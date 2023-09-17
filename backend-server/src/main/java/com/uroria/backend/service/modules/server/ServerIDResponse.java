package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ServerIDResponse extends PulsarResponse<Serverold, Integer> {
    private final BackendServerManager serverManager;

    public ServerIDResponse(@NonNull PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:request:id", "server:response:id", serverManager.getModuleName());
        this.serverManager = serverManager;
    }

    @Override
    protected Serverold response(@NonNull Integer key) {
        return serverManager.getCloudServer(key, 0).orElse(null);
    }
}
