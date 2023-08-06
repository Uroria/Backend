package com.uroria.backend.impl.permission.holder;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.permission.PermHolder;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class HolderUpdateChannel extends PulsarUpdate<PermHolder> {
    private final Consumer<PermHolder> permHolderConsumer;

    public HolderUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<PermHolder> permHolderConsumer) throws PulsarClientException {
        super(pulsarClient, "perm:holder:update", name);
        this.permHolderConsumer = permHolderConsumer;
    }

    @Override
    protected void onUpdate(PermHolder holder) {
        this.permHolderConsumer.accept(holder);
    }
}
