package com.uroria.backend.impl.player;

import com.uroria.backend.player.BackendPlayer;
import com.uroria.backend.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class BackendPlayerUUIDRequest extends PulsarRequest<BackendPlayer, UUID> {
    public BackendPlayerUUIDRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "player:request:uuid", "player:response:uuid", bridgeName, 10000, 10);
    }
}
