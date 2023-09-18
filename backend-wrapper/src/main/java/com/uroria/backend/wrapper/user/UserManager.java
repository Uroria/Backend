package com.uroria.backend.wrapper.user;

import com.uroria.backend.impl.user.AbstractUserManager;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class UserManager extends AbstractUserManager {
    public UserManager(@NonNull PulsarClient pulsarClient) {
        super(pulsarClient);
    }

    @Override
    public void start() throws PulsarClientException {

    }

    @Override
    public void shutdown() throws PulsarClientException {
        logger.info("Closing User-Module");
        this.request.close();
        this.update.close();
    }
}
