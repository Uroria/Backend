package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.List;

public final class ServerAllResponse extends PulsarResponse<List<Serverold>, Integer> {
    private final BackendServerManager serverManager;

    public ServerAllResponse(@NonNull PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:request:all", "server:response:all", serverManager.getModuleName());
        this.serverManager = serverManager;
    }

    @Override
    protected List<Serverold> response(@NonNull Integer key) {
        return this.serverManager.getServers();
    }
}
