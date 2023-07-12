package com.uroria.backend.velocity;

import com.uroria.backend.impl.message.AbstractMessageManager;
import com.uroria.backend.impl.message.BackendMessageUpdate;
import com.uroria.backend.messenger.BackendMessage;
import com.uroria.backend.velocity.events.MessageReceiveEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public final class MessageManagerImpl extends AbstractMessageManager {
    private final ProxyServer proxyServer;
    private BackendMessageUpdate messageUpdate;

    public MessageManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    protected void start(String identifier) {
        try {
            this.messageUpdate = new BackendMessageUpdate(this.pulsarClient, identifier, this::onMessage);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void shutdown() {
        try {
            if (this.messageUpdate != null) this.messageUpdate.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void onMessage(BackendMessage message) {
        this.proxyServer.getEventManager().fireAndForget(new MessageReceiveEvent(message));
    }

    @Override
    public void sendMessage(BackendMessage message) {
        if (message == null) throw new IllegalStateException("Message cannot be null");
        this.messageUpdate.update(message);
    }
}
