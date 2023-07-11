package com.uroria.backend.server.modules.permission;

import com.uroria.backend.common.permission.PermissionHolder;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendHolderUpdate extends PulsarUpdate<PermissionHolder> {
    private final BackendPermissionManager permissionManager;

    public BackendHolderUpdate(PulsarClient pulsarClient, BackendPermissionManager permissionManager) throws PulsarClientException {
        super(pulsarClient, "permission:holder:update", "PermissionModule");
        this.permissionManager = permissionManager;
    }

    @Override
    protected void onUpdate(PermissionHolder holder) {
        this.permissionManager.updateLocal(holder);
    }
}
