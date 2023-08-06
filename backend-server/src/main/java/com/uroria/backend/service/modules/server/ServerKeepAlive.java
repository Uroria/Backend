package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.ping.BackendPing;
import com.uroria.backend.impl.pulsar.PulsarKeepAliveChecker;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerStatus;
import com.uroria.backend.service.BackendServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ServerKeepAlive extends PulsarKeepAliveChecker {
    private final BackendServerManager serverManager;

    public ServerKeepAlive(PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "backend:keepalive", "Backend");
        this.serverManager = serverManager;
    }

    @Override
    protected void onTimeout(BackendPing ping) {
        Server server = this.serverManager.getServer(ping.getIdentifier()).orElse(null);
        if (server == null) return;
        this.keepAlives.remove(ping);
        server.setStatus(ServerStatus.STOPPED);
        BackendServer.getLogger().warn("Server " + ping.getIdentifier() + " timed out!");
        this.serverManager.updateServer(server);
    }
}
