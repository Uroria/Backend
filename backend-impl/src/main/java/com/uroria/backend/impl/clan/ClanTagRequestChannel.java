package com.uroria.backend.impl.clan;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.pulsar.PulsarRequest;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ClanTagRequestChannel extends PulsarRequest<Clan, String> {
    public ClanTagRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "clan:request:tag", "clan:response:tag", name, 5000);
    }
}
