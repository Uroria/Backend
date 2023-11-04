package com.uroria.backend.communication;

import com.rabbitmq.client.Channel;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.UUID;

public abstract class CommunicationPoint {
    @Getter
    protected final Communicator communicator;
    protected final Channel channel;
    protected final String topic;
    @Getter
    protected final String appId;
    protected final String queue;

    public CommunicationPoint(Communicator communicator, String topic) {
        this.communicator = communicator;
        this.topic = topic;
        this.appId = UUID.randomUUID().toString();
        try {
            this.channel = communicator.connection.createChannel();
            this.queue = this.channel.queueDeclare().getQueue();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public Logger logger() {
        return communicator.getLogger();
    }
}
