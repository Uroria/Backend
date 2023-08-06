package com.uroria.backend.impl.permission.group;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.permission.PermGroup;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class GroupUpdateChannel extends PulsarUpdate<PermGroup> {
    private final Consumer<PermGroup> groupConsumer;

    public GroupUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<PermGroup> groupConsumer) throws PulsarClientException {
        super(pulsarClient, "perm:group:update", name);
        this.groupConsumer = groupConsumer;
    }

    @Override
    protected void onUpdate(PermGroup group) {
        this.groupConsumer.accept(group);
    }
}
