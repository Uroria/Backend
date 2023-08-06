package com.uroria.backend.service.modules.user;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import com.uroria.backend.user.User;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class UserNameResponse extends PulsarResponse<User, String> {
    private final BackendUserManager userManager;

    public UserNameResponse(@NonNull PulsarClient pulsarClient, BackendUserManager userManager) throws PulsarClientException {
        super(pulsarClient, "user:request:name", "user:response:name", userManager.getModuleName());
        this.userManager = userManager;
    }

    @Override
    protected User response(@NonNull String key) {
        return this.userManager.getUser(key, 0).orElse(null);
    }
}
