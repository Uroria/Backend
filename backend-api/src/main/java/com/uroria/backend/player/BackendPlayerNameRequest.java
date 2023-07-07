package com.uroria.backend.player;

import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendPlayerNameRequest extends PulsarRequest<BackendPlayer, String> {
    public BackendPlayerNameRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "player:request:name", "player:response:name", bridgeName, 10000, 10);
    }
}
