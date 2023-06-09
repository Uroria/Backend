package com.uroria.backend.permission;

import com.uroria.backend.common.PermissionHolder;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class BackendPermissionHolderUpdate extends PulsarUpdate<PermissionHolder> {
    private final Consumer<PermissionHolder> holderConsumer;
    public BackendPermissionHolderUpdate(PulsarClient pulsarClient, String bridgeName, Consumer<PermissionHolder> holderConsumer) throws PulsarClientException {
        super(pulsarClient, "permission:holder:update", bridgeName);
        this.holderConsumer = holderConsumer;
    }

    @Override
    protected void onUpdate(PermissionHolder object) {
        holderConsumer.accept(object);
    }
}
