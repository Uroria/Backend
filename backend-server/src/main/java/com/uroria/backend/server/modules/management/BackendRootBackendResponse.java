package com.uroria.backend.server.modules.management;

import com.uroria.backend.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendRootBackendResponse extends PulsarResponse<Integer, Boolean> {

    public BackendRootBackendResponse(PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "backend:online:request", "backend:online:response", "ManagementModule");
    }

    @Override
    protected Boolean response(Integer key) {
        return true;
    }
}
