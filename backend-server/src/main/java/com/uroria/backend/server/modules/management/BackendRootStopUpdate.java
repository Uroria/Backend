package com.uroria.backend.server.modules.management;

import com.uroria.backend.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendRootStopUpdate extends PulsarUpdate<Boolean> {

    public BackendRootStopUpdate(PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "backend:stop", "ManagementModule");
    }

    @Override
    protected void onUpdate(Boolean object) {
        System.exit(0);
    }
}
