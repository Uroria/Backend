package com.uroria.backend.impl.clan;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.pulsar.PulsarUpdate;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class ClanUpdateChannel extends PulsarUpdate<Clan> {
    private final Consumer<Clan> clanConsumer;

    public ClanUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<Clan> clanConsumer) throws PulsarClientException {
        super(pulsarClient, "clan:update", name);
        this.clanConsumer = clanConsumer;
    }

    @Override
    protected void onUpdate(Clan object) {
        this.clanConsumer.accept(object);
    }
}
