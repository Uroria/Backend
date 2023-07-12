package com.uroria.backend.impl.clan;

import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendClanTagRequest extends PulsarRequest<BackendClan, String> {
    public BackendClanTagRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "clan:request:tag", "clan:response:tag", bridgeName, 20000, 50);
    }

}
