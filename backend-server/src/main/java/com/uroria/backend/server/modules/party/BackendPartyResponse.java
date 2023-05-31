package com.uroria.backend.server.modules.party;

import com.uroria.backend.common.pulsar.PulsarSender;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendPartyResponse extends PulsarSender {
    BackendPartyResponse(PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "player:response", "PlayerModule");
    }
}
