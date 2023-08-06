package com.uroria.backend.impl.stats;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.stats.Stat;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class StatUpdateChannel extends PulsarUpdate<Stat> {
    private final Consumer<Stat> statConsumer;

    public StatUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<Stat> statConsumer) throws PulsarClientException {
        super(pulsarClient, "stats:update", name);
        this.statConsumer = statConsumer;
    }

    @Override
    protected void onUpdate(Stat stat) {
        this.statConsumer.accept(stat);
    }
}
