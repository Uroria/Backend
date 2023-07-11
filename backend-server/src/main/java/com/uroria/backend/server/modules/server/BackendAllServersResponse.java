package com.uroria.backend.server.modules.server;

import com.uroria.backend.common.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.List;
import java.util.Objects;

public final class BackendAllServersResponse extends PulsarResponse<Integer, List<Integer>> {
    private final BackendServerManager serverManager;

    public BackendAllServersResponse(PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:request:all", "server:response:all", "ServerModule");
        this.serverManager = serverManager;
    }

    @Override
    protected List<Integer> response(Integer key) {
        return this.serverManager.getServers().stream().map(server -> server.getId().orElse(null)).filter(Objects::nonNull).toList();
    }
}
