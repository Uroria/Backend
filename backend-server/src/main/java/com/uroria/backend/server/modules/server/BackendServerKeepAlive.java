package com.uroria.backend.server.modules.server;

import com.uroria.backend.common.BackendServer;
import com.uroria.backend.common.helpers.ServerStatus;
import com.uroria.backend.common.pulsar.PulsarKeepAliveChecker;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendServerKeepAlive extends PulsarKeepAliveChecker<Long> {
    private final BackendServerManager serverManager;
    public BackendServerKeepAlive(PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:keepalive", "ServerModule");
        this.serverManager = serverManager;
    }

    @Override
    protected void onTimeout(Long identifier) {
        BackendServer server = this.serverManager.getServer(identifier);
        server.setStatus(ServerStatus.STOPPED);
        this.serverManager.updateServer(server);
    }
}
