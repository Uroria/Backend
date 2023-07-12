package com.uroria.backend.impl.stats;

import com.uroria.backend.helpers.StatsRequest;
import com.uroria.backend.pulsar.PulsarRequest;
import com.uroria.backend.stats.BackendStat;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.Collection;

public final class BackendStatRequest extends PulsarRequest<Collection<BackendStat>, StatsRequest> {
    public BackendStatRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "stat:request", "stat:response", bridgeName, 20000, 20);
    }
}
