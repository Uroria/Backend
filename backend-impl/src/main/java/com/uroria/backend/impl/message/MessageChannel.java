package com.uroria.backend.impl.message;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.message.Message;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class MessageChannel extends PulsarUpdate<Message> {
    private final Consumer<Message> messageConsumer;
    public MessageChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<Message> messageConsumer) throws PulsarClientException {
        super(pulsarClient, "message:update", name);
        this.messageConsumer = messageConsumer;
    }

    @Override
    protected void onUpdate(Message message) {
        this.messageConsumer.accept(message);
    }
}
