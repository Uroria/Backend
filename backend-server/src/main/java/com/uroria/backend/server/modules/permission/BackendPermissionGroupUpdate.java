package com.uroria.backend.server.modules.permission;

import com.uroria.backend.common.PermissionGroup;
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

public final class BackendPermissionGroupUpdate extends PulsarBridge {
    private final Logger logger;
    private final BackendPermissionManager permissionManager;
    BackendPermissionGroupUpdate(Logger logger, BackendPermissionManager permissionManager, PulsarClient pulsarClient) throws PulsarClientException {
        super(pulsarClient, "permissiongroup:update", "PermissionModule", 100000);
        this.logger = logger;
        this.permissionManager = permissionManager;
    }

    @Override
    public void onReceive(Consumer<byte[]> consumer, Message<byte[]> message) throws PulsarClientException {
        consumer.acknowledge(message);
        if (message.getProducerName().equals(this.bridgeName)) return;
        CompletableFuture.runAsync(() -> {
            PermissionGroup group;
            try {
                ByteArrayInputStream inputBuffer = new ByteArrayInputStream(message.getData());
                ObjectInputStream input = new ObjectInputStream(inputBuffer);
                group = (PermissionGroup) input.readObject();
                input.close();
                inputBuffer.close();
            } catch (Exception exception) {
                this.logger.error("Cannot update player by " + message.getProducerName(), exception);
                Uroria.captureException(exception);
                return;
            }
            this.permissionManager.updateGroup(group);
        });
    }
}
