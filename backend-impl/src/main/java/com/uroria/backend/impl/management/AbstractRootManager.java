package com.uroria.backend.impl.management;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.management.RootManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractRootManager extends AbstractManager implements RootManager {

    public AbstractRootManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
    }

    abstract protected void checkStopEverything();
}
