package com.uroria.backend.service.modules.message;

import com.uroria.backend.impl.message.MessageChannel;
import com.uroria.backend.message.Message;
import com.uroria.backend.message.MessageManager;
import com.uroria.backend.service.modules.AbstractManager;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendMessageManager extends AbstractManager implements MessageManager {
    private MessageChannel channel;

    public BackendMessageManager(PulsarClient pulsarClient) {
        super(pulsarClient, "MessageModule");
    }

    @Override
    public void enable() throws PulsarClientException {
        this.channel = new MessageChannel(this.pulsarClient, getModuleName(), message -> {});
    }

    @Override
    public void disable() throws PulsarClientException {
        if (this.channel != null) this.channel.close();
    }

    @Override
    public void sendMessage(@NonNull Message message) {
        this.channel.update(message);
    }
}
