package com.uroria.backend.service.modules.root;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendResponse extends PulsarResponse<Boolean, Long> {
    public BackendResponse(@NonNull PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "backend:online:request", "backend:online:response", "RootModule");
    }

    @Override
    protected Boolean response(@NonNull Long key) {
        return true;
    }
}
