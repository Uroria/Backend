package com.uroria.backend.service.modules.permission;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import com.uroria.backend.permission.PermHolder;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class HolderResponse extends PulsarResponse<PermHolder, UUID> {
    private final BackendPermManager permManager;

    public HolderResponse(@NonNull PulsarClient pulsarClient, BackendPermManager permManager) throws PulsarClientException {
        super(pulsarClient, "perm:holder:request", "perm:holder:response", permManager.getModuleName());
        this.permManager = permManager;
    }

    @Override
    protected PermHolder response(@NonNull UUID key) {
        return this.permManager.getHolder(key, 0).orElse(null);
    }
}
