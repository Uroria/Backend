package com.uroria.backend.communication;

import com.google.gson.Gson;
import com.rabbitmq.client.Connection;
import com.uroria.backend.communication.messenger.RabbitUtils;
import com.uroria.base.gson.GsonFactory;
import org.slf4j.Logger;

public abstract class Communicator {
    private static final Gson gson = GsonFactory.create();
    protected final Logger logger;
    protected final Connection connection;

    public Communicator(Logger logger) {
        this.logger = logger;
        this.connection = RabbitUtils.buildConnection(logger);
    }

    public Logger getLogger() {
        return logger;
    }

    public Connection getConnection() {
        return connection;
    }

    public static Gson getGson() {
        return gson;
    }
}
