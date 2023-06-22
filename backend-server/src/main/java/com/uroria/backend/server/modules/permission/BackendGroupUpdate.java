package com.uroria.backend.server.modules.permission;

import com.uroria.backend.common.PermissionGroup;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendGroupUpdate extends PulsarUpdate<PermissionGroup> {
    private final BackendPermissionManager permissionManager;
    public BackendGroupUpdate(PulsarClient pulsarClient, BackendPermissionManager permissionManager) throws PulsarClientException {
        super(pulsarClient, "permission:group:update", "PermissionModule");
        this.permissionManager = permissionManager;
    }

    @Override
    protected void onUpdate(PermissionGroup group) {
        this.permissionManager.updateLocal(group);
    }
}
