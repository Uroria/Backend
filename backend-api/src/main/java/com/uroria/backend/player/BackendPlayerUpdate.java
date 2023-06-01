package com.uroria.backend.player;

import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.common.pulsar.PulsarBridge;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.CompletableFuture;

public final class BackendPlayerUpdate extends PulsarBridge {
    private final Logger logger;
    private final PlayerManager playerManager;

    public BackendPlayerUpdate(PulsarClient pulsarClient, String bridgeName, Logger logger, PlayerManager playerManager) throws PulsarClientException {
        super(pulsarClient, "player:update", bridgeName, 10000);
        this.logger = logger;
        this.playerManager = playerManager;
    }

    @Override
    public void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException {
        consumer.acknowledge(message);
        if (message.getProducerName().equals(this.bridgeName)) return;
        CompletableFuture.runAsync(() -> {
            BackendPlayer player;
            try {
                ByteArrayInputStream inputBuffer = new ByteArrayInputStream(message.getData());
                ObjectInputStream input = new ObjectInputStream(inputBuffer);
                player = (BackendPlayer) input.readObject();
                input.close();
                inputBuffer.close();
            } catch (Exception exception) {
                this.logger.error("Cannot update player by " + message.getProducerName(), exception);
                return;
            }
            this.playerManager.checkPlayer(player);
        });
    }

    public void updatePlayer(BackendPlayer player) throws IOException {
        ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
        ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
        output.writeObject(player);
        output.close();
        outputBuffer.close();
        send(outputBuffer.toByteArray());
    }
}
