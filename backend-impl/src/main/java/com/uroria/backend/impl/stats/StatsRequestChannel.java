package com.uroria.backend.impl.stats;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.stats.Stat;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.Collection;

public final class StatsRequestChannel extends PulsarRequest<Collection<Stat>, StatsRequest> {
    public StatsRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "stats:request", "stats:response", name, 10000);
    }
}
