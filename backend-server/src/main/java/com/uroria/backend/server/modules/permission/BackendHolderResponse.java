package com.uroria.backend.server.modules.permission;

import com.uroria.backend.common.PermissionHolder;
import com.uroria.backend.common.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class BackendHolderResponse extends PulsarResponse<UUID, PermissionHolder> {
    private final BackendPermissionManager permissionManager;
    public BackendHolderResponse(PulsarClient pulsarClient, BackendPermissionManager permissionManager) throws PulsarClientException {
        super(pulsarClient, "permission:holder:request", "permission:holder:response", "PermissionModule");
        this.permissionManager = permissionManager;
    }

    @Override
    protected PermissionHolder response(UUID key) {
        return this.permissionManager.getHolder(key).orElse(null);
    }
}
