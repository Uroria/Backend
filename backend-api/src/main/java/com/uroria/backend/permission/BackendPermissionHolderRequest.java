package com.uroria.backend.permission;

import com.uroria.backend.common.PermissionHolder;
import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class BackendPermissionHolderRequest extends PulsarRequest<PermissionHolder, UUID> {
    public BackendPermissionHolderRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "permission:holder:request", "permission:holder:response", bridgeName, 5000, 10);
    }

}
