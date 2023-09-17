package com.uroria.backend.service.modules.user;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class UserUUIDResponse extends PulsarResponse<UserOld, UUID> {
    private final BackendUserManager userManager;

    public UserUUIDResponse(@NonNull PulsarClient pulsarClient, BackendUserManager userManager) throws PulsarClientException {
        super(pulsarClient, "user:request:uuid", "user:response:uuid", userManager.getModuleName());
        this.userManager = userManager;
    }

    @Override
    protected UserOld response(@NonNull UUID key) {
        return this.userManager.getUser(key, 0).orElse(null);
    }
}
