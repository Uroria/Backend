package com.uroria.backend.server.modules.stats;

import com.uroria.backend.common.pulsar.PulsarSender;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendStatResponse extends PulsarSender {
    BackendStatResponse(PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "stats:response", "StatsModule");
    }
}
