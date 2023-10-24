package com.uroria.backend.impl;

import com.rabbitmq.client.Connection;
import com.uroria.backend.BackendWrapper;
import com.uroria.backend.communication.Communicator;
import com.uroria.base.event.EventManager;
import com.uroria.base.event.EventManagerFactory;
import lombok.NonNull;
import org.slf4j.Logger;

public abstract class AbstractBackendWrapper implements BackendWrapper {
    protected final Logger logger;
    protected final Communicator communicator;
    private final EventManager eventManager;

    protected AbstractBackendWrapper(@NonNull Logger logger) {
        this.logger = logger;
        this.communicator = new Communicator(logger);
        this.eventManager = EventManagerFactory.create("BackendEvents");
    }

    @Override
    public final EventManager getEventManager() {
        return this.eventManager;
    }

    abstract public void start() throws Exception;

    public void shutdown() throws Exception {
        this.communicator.close();
    }

    public Logger getLogger() {
        return this.logger;
    }
}
