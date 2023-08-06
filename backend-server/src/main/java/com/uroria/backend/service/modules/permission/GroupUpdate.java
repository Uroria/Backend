package com.uroria.backend.service.modules.permission;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.permission.PermGroup;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class GroupUpdate extends PulsarUpdate<PermGroup> {
    private final BackendPermManager permManager;

    public GroupUpdate(@NonNull PulsarClient pulsarClient, BackendPermManager permManager) throws PulsarClientException {
        super(pulsarClient, "perm:group:update", permManager.getModuleName());
        this.permManager = permManager;
    }

    @Override
    protected void onUpdate(PermGroup group) {
        this.permManager.updateDatabase(group);
    }
}
