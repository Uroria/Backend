package com.uroria.backend.impl.clan;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.pulsar.PulsarRequest;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class ClanOperatorRequestChannel extends PulsarRequest<Clan, UUID> {
    public ClanOperatorRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "clan:request:operator", "clan:response:operator", name, 5000);
    }
}
