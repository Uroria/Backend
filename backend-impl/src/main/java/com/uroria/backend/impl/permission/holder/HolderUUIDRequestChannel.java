package com.uroria.backend.impl.permission.holder;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.permission.PermHolder;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class HolderUUIDRequestChannel extends PulsarRequest<PermHolder, UUID> {
    public HolderUUIDRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "perm:holder:request", "perm:holder:response", name, 3000);
    }
}
