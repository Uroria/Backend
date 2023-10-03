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

    protected AbstractBackendWrapper(@NonNull Logger logger) throws Exception {
        this.logger = logger;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(RabbitConfiguration.getRabbitUsername());
        factory.setPassword(RabbitConfiguration.getRabbitPassword());
        factory.setVirtualHost(RabbitConfiguration.getRabbitVirtualhost());
        factory.setHost(RabbitConfiguration.getRabbitHostname());
        factory.setPort(RabbitConfiguration.getRabbitPort());
        if (RabbitConfiguration.isRabbitSslEnabled()) factory.useSslProtocol();
        this.connection = factory.newConnection();
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
