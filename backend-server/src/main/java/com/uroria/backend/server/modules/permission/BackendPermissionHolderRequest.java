package com.uroria.backend.server.modules.permission;

import com.uroria.backend.common.PermissionHolder;
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

public final class BackendPermissionHolderRequest extends PulsarReceiver {
    private final Logger logger;
    private final BackendPermissionManager permissionManager;
    BackendPermissionHolderRequest(PulsarClient pulsarClient, Logger logger, BackendPermissionManager permissionManager) throws PulsarClientException {
        super(pulsarClient, "permissionholder:request", "PermissionModule", 100000);
        this.logger = logger;
        this.permissionManager = permissionManager;
    }

    @Override
    public void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException {
        consumer.acknowledge(message);
        UUID uuid = null;
        try {
            ByteArrayInputStream inputBuffer = new ByteArrayInputStream(message.getData());
            ObjectInputStream input = new ObjectInputStream(inputBuffer);
            uuid = (UUID) input.readObject();
            input.close();
            inputBuffer.close();
        } catch (Exception exception) {
            this.logger.error("Cannot read data from " + message.getProducerName(), exception);
            Uroria.captureException(exception);
        }

        PermissionHolder holder = this.permissionManager.getHolder(uuid).orElse(null);
        this.logger.debug("Requesting stat with uuid " + uuid);

        try {
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
            output.writeObject(uuid);
            IOUtils.writeObject(output, holder);
            output.close();
            outputBuffer.close();
            this.permissionManager.getHolderResponseSender().send(outputBuffer.toByteArray());
        } catch (Exception exception) {
            this.logger.error("Cannot write data for " + message.getProducerName(), exception);
            Uroria.captureException(exception);
        }
    }
}
