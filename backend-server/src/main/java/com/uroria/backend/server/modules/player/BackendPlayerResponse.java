package com.uroria.backend.server.modules.player;

import com.uroria.backend.common.pulsar.PulsarSender;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendPlayerResponse extends PulsarSender {
    BackendPlayerResponse(PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "player:response", "PlayerModule");
    }
}
