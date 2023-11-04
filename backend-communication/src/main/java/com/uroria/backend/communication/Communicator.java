package com.uroria.backend.communication;

import com.google.gson.Gson;
import com.rabbitmq.client.Connection;
import com.uroria.backend.communication.messenger.RabbitUtils;
import com.uroria.base.gson.GsonFactory;
import lombok.Getter;
import org.slf4j.Logger;

public class Communicator {
    @Getter
    private static final Gson gson = GsonFactory.create();
    protected final Logger logger;
    protected final Connection connection;

    public Communicator(Logger logger) {
        this.logger = logger;
        this.connection = RabbitUtils.buildConnection(logger);
    }

    public void close() {
        try {
            this.connection.close();
        } catch (Exception exception) {
            logger.error("Unable to close connection to rabbitmq", exception);
        }
    }

    public final Logger getLogger() {
        return logger;
    }

    public final Connection getConnection() {
        return connection;
    }
}
