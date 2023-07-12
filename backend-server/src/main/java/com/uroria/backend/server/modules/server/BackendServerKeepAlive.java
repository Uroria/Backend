package com.uroria.backend.server.modules.server;

import com.uroria.backend.BackendPing;
import com.uroria.backend.server.BackendServer;
import com.uroria.backend.server.ServerStatus;
import com.uroria.backend.pulsar.PulsarKeepAliveChecker;
import com.uroria.backend.server.Uroria;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendServerKeepAlive extends PulsarKeepAliveChecker {
    private final BackendServerManager serverManager;
    public BackendServerKeepAlive(PulsarClient pulsarClient, BackendServerManager serverManager) throws PulsarClientException {
        super(pulsarClient, "server:keepalive", "ServerModule");
        this.serverManager = serverManager;
    }

    @Override
    protected void onTimeout(BackendPing ping) {
        BackendServer server = this.serverManager.getServer(ping.getIdentifier());
        if (server == null) {
            this.keepAlives.remove(ping);
            return;
        }
        server.setStatus(ServerStatus.STOPPED);
        Uroria.getLogger().warn("Server " + ping.getIdentifier() + " timed out!");
        this.serverManager.updateServer(server);
    }
}
