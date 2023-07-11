package com.uroria.backend.server.modules.permission;

import com.uroria.backend.common.permission.PermissionGroup;
import com.uroria.backend.common.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendGroupResponse extends PulsarResponse<String, PermissionGroup> {
    private final BackendPermissionManager permissionManager;
    public BackendGroupResponse(PulsarClient pulsarClient, BackendPermissionManager permissionManager) throws PulsarClientException {
        super(pulsarClient, "permission:group:request", "permission:group:response", "PermissionModule");
        this.permissionManager = permissionManager;
    }

    @Override
    protected PermissionGroup response(String key) {
        return this.permissionManager.getGroup(key).orElse(null);
    }
}
