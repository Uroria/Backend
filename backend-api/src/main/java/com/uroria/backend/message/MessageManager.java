package com.uroria.backend.message;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.BackendMessage;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class MessageManager extends AbstractManager {
    protected final Logger logger;
    public MessageManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
    }

    abstract protected void onMessage(BackendMessage message);

    abstract public void sendMessage(BackendMessage message);
}
