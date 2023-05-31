package com.uroria.backend.server.modules.player;

import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.common.pulsar.PulsarReceiver;
import com.uroria.backend.common.utils.IOUtils;
import com.uroria.backend.server.Uroria;
import org.apache.pulsar.client.api.Consumer;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

public final class BackendPlayerRequest extends PulsarReceiver {
    private final Logger logger;
    private final BackendPlayerManager playerManager;
    BackendPlayerRequest(PulsarClient pulsarClient, Logger logger, BackendPlayerManager playerManager) throws PulsarClientException {
        super(pulsarClient, "player:request", "PlayerModule", 100000);
        this.logger = logger;
        this.playerManager = playerManager;
    }

    @Override
    public void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException {
        consumer.acknowledge(message);
        UUID uuid = null;
        String name = null;
        try {
            ByteArrayInputStream inputBuffer = new ByteArrayInputStream(message.getData());
            ObjectInputStream input = new ObjectInputStream(inputBuffer);
            if (input.readBoolean()) uuid = (UUID) input.readObject();
            else name = input.readUTF();
            input.close();
            inputBuffer.close();
        } catch (Exception exception) {
            this.logger.error("Cannot read data from " + message.getProducerName(), exception);
            Uroria.captureException(exception);
        }

        BackendPlayer player = null;
        if (uuid != null) {
            this.logger.debug("Requesting player with uuid " + uuid);
            player = this.playerManager.getPlayer(uuid).orElse(null);
        } else {
            if (name != null) {
                this.logger.debug("Requesting player with name " + name);
                player = this.playerManager.getPlayer(name).orElse(null);
            }
        }

        try {
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
            if (uuid == null) {
                output.writeBoolean(false);
                output.writeUTF(name);
            } else {
                output.writeBoolean(true);
                output.writeObject(uuid);
            }
            IOUtils.writeObject(output, player);
            output.close();
            outputBuffer.close();
            this.playerManager.getResponseSender().send(outputBuffer.toByteArray());
        } catch (Exception exception) {
            this.logger.error("Cannot write data for " + message.getProducerName(), exception);
            Uroria.captureException(exception);
        }
    }
}
