package com.uroria.backend.impl.message;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.messenger.BackendMessage;
import com.uroria.backend.messenger.MessageManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractMessageManager extends AbstractManager implements MessageManager {
    public AbstractMessageManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
    }

    abstract protected void onMessage(BackendMessage message);
}
