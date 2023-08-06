package com.uroria.backend.service.modules.permission;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import com.uroria.backend.permission.PermGroup;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class GroupResponse extends PulsarResponse<PermGroup, String> {
    private final BackendPermManager permManager;

    public GroupResponse(@NonNull PulsarClient pulsarClient, BackendPermManager permManager) throws PulsarClientException {
        super(pulsarClient, "perm:group:request", "perm:group:response", permManager.getModuleName());
        this.permManager = permManager;
    }

    @Override
    protected PermGroup response(@NonNull String key) {
        return this.permManager.getGroup(key, 0).orElse(null);
    }
}
