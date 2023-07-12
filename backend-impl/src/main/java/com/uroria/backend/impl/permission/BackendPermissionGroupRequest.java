package com.uroria.backend.impl.permission;

import com.uroria.backend.permission.PermissionGroup;
import com.uroria.backend.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendPermissionGroupRequest extends PulsarRequest<PermissionGroup, String> {
    public BackendPermissionGroupRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "permission:group:request", "permission:group:response", bridgeName, 10000, 10);
    }
}
