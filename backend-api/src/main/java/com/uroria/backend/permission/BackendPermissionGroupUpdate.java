package com.uroria.backend.permission;

import com.uroria.backend.common.PermissionGroup;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class BackendPermissionGroupUpdate extends PulsarUpdate<PermissionGroup> {
    private final Consumer<PermissionGroup> groupConsumer;
    public BackendPermissionGroupUpdate(PulsarClient pulsarClient, String bridgeName, Consumer<PermissionGroup> groupConsumer) throws PulsarClientException {
        super(pulsarClient, "permission:group:update", bridgeName);
        this.groupConsumer = groupConsumer;
    }

    @Override
    protected void onUpdate(PermissionGroup object) {
        groupConsumer.accept(object);
    }
}
