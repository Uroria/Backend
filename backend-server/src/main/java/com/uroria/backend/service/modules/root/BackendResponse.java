package com.uroria.backend.service.modules.root;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

public final class BackendResponse extends PulsarResponse<Boolean, Long> {
    private final Logger logger;
    public BackendResponse(@NonNull PulsarClient pulsarClient, Logger logger) throws PulsarClientException {
        super(pulsarClient, "backend:online:request", "backend:online:response", "RootModule");
        this.logger = logger;
    }

    @Override
    protected Boolean response(@NonNull Long key) {
        logger.info("Online check from " + key);
        return true;
    }
}
