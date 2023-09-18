package com.uroria.backend.service.modules.user;

import com.uroria.backend.impl.pulsarold.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class UserUpdate extends PulsarUpdate<UserOld> {
    private final BackendUserManager userManager;

    public UserUpdate(PulsarClient pulsarClient, BackendUserManager userManager) throws PulsarClientException {
        super(pulsarClient, "user:update", userManager.getModuleName());
        this.userManager = userManager;
    }

    @Override
    protected void onUpdate(UserOld user) {
        this.userManager.updateDatabase(user);
    }
}
