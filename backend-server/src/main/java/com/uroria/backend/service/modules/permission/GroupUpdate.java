package com.uroria.backend.service.modules.permission;

import com.uroria.backend.impl.pulsarold.PulsarUpdate;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class GroupUpdate extends PulsarUpdate<PermGroupOld> {
    private final BackendPermManager permManager;

    public GroupUpdate(@NonNull PulsarClient pulsarClient, BackendPermManager permManager) throws PulsarClientException {
        super(pulsarClient, "perm:group:update", permManager.getModuleName());
        this.permManager = permManager;
    }

    @Override
    protected void onUpdate(PermGroupOld group) {
        this.permManager.updateDatabase(group);
    }
}
