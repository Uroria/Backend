package com.uroria.backend.service.modules.permission;

import com.uroria.backend.impl.pulsarold.PulsarUpdate;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class HolderUpdate extends PulsarUpdate<PermHolderOld> {
    private final BackendPermManager permManager;

    public HolderUpdate(@NonNull PulsarClient pulsarClient, BackendPermManager permManager) throws PulsarClientException {
        super(pulsarClient, "perm:holder:update", permManager.getModuleName());
        this.permManager = permManager;
    }

    @Override
    protected void onUpdate(PermHolderOld holder) {
        this.permManager.updateDatabase(holder);
    }
}
