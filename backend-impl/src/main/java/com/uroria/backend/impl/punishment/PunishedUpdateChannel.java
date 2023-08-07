package com.uroria.backend.impl.punishment;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.punishment.Punished;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class PunishedUpdateChannel extends PulsarUpdate<Punished> {
    private final Consumer<Punished> punishedConsumer;

    public PunishedUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<Punished> punishedConsumer) throws PulsarClientException {
        super(pulsarClient, "punished:update", name);
        this.punishedConsumer = punishedConsumer;
    }

    @Override
    protected void onUpdate(Punished punished) {
        this.punishedConsumer.accept(punished);
    }
}
