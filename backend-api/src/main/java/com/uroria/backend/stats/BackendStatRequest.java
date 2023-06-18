package com.uroria.backend.stats;

import com.uroria.backend.common.BackendStat;
import com.uroria.backend.common.helpers.StatsRequest;
import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.Collection;

public final class BackendStatRequest extends PulsarRequest<Collection<BackendStat>, StatsRequest> {
    public BackendStatRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "stat:request", "stat:response", bridgeName, 20000, 20);
    }

    @Override
    protected void onRequest(StatsRequest key) {
        LOGGER.info("Requesting stats with game-id " + key.getGameId());
    }
}
