package com.uroria.backend.clan;

import com.uroria.backend.common.BackendClan;
import com.uroria.backend.common.pulsar.PulsarUpdate;
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
        LOGGER.info("Updating clan with tag " + object.getTag() + " and name " + object.getName());
        this.clanConsumer.accept(object);
    }
}
