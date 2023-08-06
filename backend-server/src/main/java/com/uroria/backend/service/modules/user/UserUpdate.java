package com.uroria.backend.service.modules.user;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.user.User;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class UserUpdate extends PulsarUpdate<User> {
    private final BackendUserManager userManager;

    public UserUpdate(PulsarClient pulsarClient, BackendUserManager userManager) throws PulsarClientException {
        super(pulsarClient, "user:update", userManager.getModuleName());
        this.userManager = userManager;
    }

    @Override
    protected void onUpdate(User user) {
        this.userManager.updateDatabase(user);
    }
}
