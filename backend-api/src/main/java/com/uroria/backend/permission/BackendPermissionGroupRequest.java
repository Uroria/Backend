package com.uroria.backend.permission;

import com.uroria.backend.common.PermissionGroup;
import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class BackendPermissionGroupRequest extends PulsarRequest<PermissionGroup, String> {
    public BackendPermissionGroupRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "permission:group:request", "permission:group:response", bridgeName, 10000, 10);
    }
}