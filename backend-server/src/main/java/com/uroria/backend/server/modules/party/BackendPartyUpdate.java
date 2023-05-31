package com.uroria.backend.server.modules.party;

import com.uroria.backend.common.BackendParty;
import com.uroria.backend.common.pulsar.PulsarBridge;
import com.uroria.backend.server.Uroria;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.CompletableFuture;

public final class BackendPartyUpdate extends PulsarBridge {
    private final Logger logger;
    private final BackendPartyManager partyManager;
    BackendPartyUpdate(Logger logger, BackendPartyManager partyManager, PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "player:update", "PlayerModule", 100000);
        this.logger = logger;
        this.partyManager = partyManager;
    }

    @Override
    public void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException {
        consumer.acknowledge(message);
        if (message.getProducerName().equals(this.bridgeName)) return;
        CompletableFuture.runAsync(() -> {
            BackendParty party;
            try {
                ByteArrayInputStream inputBuffer = new ByteArrayInputStream(message.getData());
                ObjectInputStream input = new ObjectInputStream(inputBuffer);
                party = (BackendParty) input.readObject();
                input.close();
                inputBuffer.close();
            } catch (Exception exception) {
                this.logger.error("Cannot update player by " + message.getProducerName(), exception);
                Uroria.captureException(exception);
                return;
            }
            this.partyManager.updateParty(party);
        });
    }
}
