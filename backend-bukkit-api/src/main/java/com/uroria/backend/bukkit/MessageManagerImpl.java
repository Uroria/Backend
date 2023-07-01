package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.events.MessageReceiveEvent;
import com.uroria.backend.common.BackendMessage;
import com.uroria.backend.message.BackendMessageUpdate;
import com.uroria.backend.message.MessageManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

public class MessageManagerImpl extends MessageManager {
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
            BackendAPI.captureException(exception);
        }
    }

    @Override
    protected void shutdown() {
        try {
            if (this.messageUpdate != null) this.messageUpdate.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPI.captureException(exception);
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
