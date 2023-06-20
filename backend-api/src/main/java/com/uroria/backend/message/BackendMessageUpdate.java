package com.uroria.backend.message;

import com.uroria.backend.common.BackendMessage;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class BackendMessageUpdate extends PulsarUpdate<BackendMessage> {
    private final Consumer<BackendMessage> messageConsumer;
    public BackendMessageUpdate(PulsarClient pulsarClient, String bridgeName, Consumer<BackendMessage> messageConsumer) throws PulsarClientException {
        super(pulsarClient, "message", bridgeName);
        this.messageConsumer = messageConsumer;
    }

    @Override
    protected void onUpdate(BackendMessage object) {
        this.messageConsumer.accept(object);
    }
}
