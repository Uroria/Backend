package com.uroria.backend.impl.message;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.message.Message;
import com.uroria.backend.message.MessageManager;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractMessageManager extends AbstractManager implements MessageManager {
    public AbstractMessageManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
    }

    protected abstract void checkMessage(@NonNull Message message);
}
