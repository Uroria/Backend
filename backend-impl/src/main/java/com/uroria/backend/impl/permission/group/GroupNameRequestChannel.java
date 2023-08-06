package com.uroria.backend.impl.permission.group;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.permission.PermGroup;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class GroupNameRequestChannel extends PulsarRequest<PermGroup, String> {
    public GroupNameRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "perm:group:request", "perm:group:response", name, 5000);
    }
}
