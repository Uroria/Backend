package com.uroria.backend.clan;

import com.uroria.backend.common.BackendClan;
import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendClanTagRequest extends PulsarRequest<BackendClan, String> {
    public BackendClanTagRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "clan:request:tag", "clan:response:tag", bridgeName, 20000, 50);
    }
}
