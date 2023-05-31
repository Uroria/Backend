package com.uroria.backend.server.modules.permission;

import com.uroria.backend.common.PermissionGroup;
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

public final class BackendPermissionGroupRequest extends PulsarReceiver {
    private final Logger logger;
    private final BackendPermissionManager permissionManager;
    BackendPermissionGroupRequest(PulsarClient pulsarClient, Logger logger, BackendPermissionManager permissionManager) throws PulsarClientException {
        super(pulsarClient, "permissiongroup:request", "PermissionModule", 100000);
        this.logger = logger;
        this.permissionManager = permissionManager;
    }
    @Override
    public void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException {
        consumer.acknowledge(message);
        String name = null;
        try {
            ByteArrayInputStream inputBuffer = new ByteArrayInputStream(message.getData());
            ObjectInputStream input = new ObjectInputStream(inputBuffer);
            name = input.readUTF();
            input.close();
            inputBuffer.close();
        } catch (Exception exception) {
            this.logger.error("Cannot read data from " + message.getProducerName(), exception);
            Uroria.captureException(exception);
        }

        PermissionGroup group = this.permissionManager.getGroup(name).orElse(null);
        this.logger.debug("Requesting permissiongroup with name " + name);

        try {
            ByteArrayOutputStream outputBuffer = new ByteArrayOutputStream();
            ObjectOutputStream output = new ObjectOutputStream(outputBuffer);
            output.writeUTF(name);
            IOUtils.writeObject(output, group);
            output.close();
            outputBuffer.close();
            this.permissionManager.getGroupResponseSender().send(outputBuffer.toByteArray());
        } catch (Exception exception) {
            this.logger.error("Cannot write data for " + message.getProducerName(), exception);
            Uroria.captureException(exception);
        }
    }
}
