package com.uroria.backend.impl;

import com.rabbitmq.client.Connection;
import org.slf4j.Logger;

public abstract class AbstractManager {
    protected final Connection rabbit;
    protected final Logger logger;

    public AbstractManager(Connection rabbit, Logger logger) {
        this.rabbit = rabbit;
        this.logger = logger;
    }

    abstract protected void start() throws Exception;

    abstract protected void shutdown() throws Exception;
}
