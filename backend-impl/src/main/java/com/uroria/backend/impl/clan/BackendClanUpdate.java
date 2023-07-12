package com.uroria.backend.impl.clan;

import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class BackendClanUpdate extends PulsarUpdate<BackendClan> {
    private final Consumer<BackendClan> clanConsumer;

    public BackendClanUpdate(PulsarClient pulsarClient, String bridgeName, Consumer<BackendClan> clanConsumer) throws PulsarClientException {
        super(pulsarClient, "clan:update", bridgeName);
        this.clanConsumer = clanConsumer;
    }

    @Override
    protected void onUpdate(BackendClan object) {
        this.clanConsumer.accept(object);
    }
}
