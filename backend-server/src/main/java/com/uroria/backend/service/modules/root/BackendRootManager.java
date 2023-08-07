package com.uroria.backend.service.modules.root;

import com.uroria.backend.impl.root.StopUpdateChannel;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.AbstractManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendRootManager extends AbstractManager {
    private final BackendServer server;
    private BackendResponse response;
    private StopUpdateChannel updateChannel;

    public BackendRootManager(PulsarClient pulsarClient, BackendServer server) {
        super(pulsarClient, "RootModule");
        this.server = server;
    }

    @Override
    public void enable() throws PulsarClientException {
        this.response = new BackendResponse(this.pulsarClient);
        this.updateChannel = new StopUpdateChannel(this.pulsarClient, getModuleName(), server::explicitShutdown);
    }

    @Override
    public void disable() throws PulsarClientException {
        if (this.response != null) this.response.close();
        if (this.updateChannel != null) this.updateChannel.close();
    }

    public void shutdownAll() {
        this.updateChannel.update(true);
    }
}
