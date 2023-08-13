package com.uroria.backend.velocity.message;

import com.uroria.backend.impl.message.AbstractMessageManager;
import com.uroria.backend.impl.message.MessageChannel;
import com.uroria.backend.message.Message;
import com.uroria.backend.message.MessageManager;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

public final class MessageManagerImpl extends AbstractMessageManager implements MessageManager {
    private final ProxyServer proxyServer;
    private MessageChannel channel;

    public MessageManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.channel = new MessageChannel(this.pulsarClient, identifier, this::checkMessage);
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.channel != null) this.channel.close();
    }

    @Override
    protected void checkMessage(@NonNull Message message) {
        MessageReceiveEvent messageReceiveEvent = new MessageReceiveEvent(message);
        this.proxyServer.getEventManager().fireAndForget(messageReceiveEvent);
    }

    @Override
    public void sendMessage(@NonNull Message message) {
        this.channel.update(message);
    }
}
