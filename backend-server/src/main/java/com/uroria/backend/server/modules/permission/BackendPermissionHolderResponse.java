package com.uroria.backend.server.modules.permission;

import com.uroria.backend.common.pulsar.PulsarSender;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendPermissionHolderResponse extends PulsarSender {
    BackendPermissionHolderResponse(PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "permissionholder:response", "PermissionModule");
    }
}
