package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.events.MessageReceiveEvent;
import com.uroria.backend.impl.message.AbstractMessageManager;
import com.uroria.backend.impl.message.BackendMessageUpdate;
import com.uroria.backend.messenger.BackendMessage;
import org.apache.pulsar.client.api.PulsarClient;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

public final class MessageManagerImpl extends AbstractMessageManager {
    private BackendMessageUpdate messageUpdate;

    MessageManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
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
        Bukkit.getPluginManager().callEvent(new MessageReceiveEvent(message));
    }

    @Override
    public void sendMessage(BackendMessage message) {
        if (BackendBukkitPlugin.isOffline()) return;
        this.messageUpdate.update(message);
    }
}
