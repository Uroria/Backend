package com.uroria.backend.impl;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.uroria.backend.BackendWrapper;
import com.uroria.backend.impl.configurations.RabbitConfiguration;
import com.uroria.base.event.EventManager;
import com.uroria.base.event.EventManagerFactory;
import lombok.NonNull;
import org.slf4j.Logger;

public abstract class AbstractBackendWrapper implements BackendWrapper {
    protected final Logger logger;
    private final Connection connection;
    private final EventManager eventManager;

    protected AbstractBackendWrapper(@NonNull Logger logger, @NonNull Connection connection) {
        this.logger = logger;
        this.connection = connection;
        this.eventManager = EventManagerFactory.create("BackendEvents");
    }

    protected AbstractBackendWrapper(@NonNull Logger logger) {
        this.logger = logger;
        this.connection = RabbitUtils.buildConnection(logger);
        this.eventManager = EventManagerFactory.create("BackendEvents");
    }

    @Override
    public final EventManager getEventManager() {
        return this.eventManager;
    }

    abstract public void start() throws Exception;

    public void shutdown() throws Exception {
        this.connection.close();
    }

    public Logger getLogger() {
        return this.logger;
    }

    public Connection getRabbit() {
        return this.connection;
    }
}
