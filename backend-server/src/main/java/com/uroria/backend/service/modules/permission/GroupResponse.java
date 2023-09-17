package com.uroria.backend.service.modules.permission;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class GroupResponse extends PulsarResponse<PermGroupOld, String> {
    private final BackendPermManager permManager;

    public GroupResponse(@NonNull PulsarClient pulsarClient, BackendPermManager permManager) throws PulsarClientException {
        super(pulsarClient, "perm:group:request", "perm:group:response", permManager.getModuleName());
        this.permManager = permManager;
    }

    @Override
    protected PermGroupOld response(@NonNull String key) {
        return this.permManager.getGroup(key, 0).orElse(null);
    }
}
