package com.uroria.backend.impl.clan;

import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class BackendClanOperatorRequest extends PulsarRequest<BackendClan, UUID> {
    public BackendClanOperatorRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "clan:request:operator", "clan:response:operator", bridgeName, 20000, 50);
    }

}
