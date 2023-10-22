package com.uroria.backend.communication;

import com.rabbitmq.client.Channel;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import org.slf4j.Logger;

import java.util.UUID;

public abstract class CommunicationPoint {
    protected final Communicator communicator;
    protected final Channel channel;
    protected final String appId;
    protected final String topic;
    protected final String queue;

    public CommunicationPoint(Communicator communicator, String topic) {
        this.communicator = communicator;
        this.topic = topic;
        this.appId = UUID.randomUUID().toString();
        try {
            this.channel = communicator.connection.createChannel();
            this.queue = this.channel.queueDeclare(topic, false, false, true, Object2ObjectMaps.emptyMap()).getQueue();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    protected Logger logger() {
        return communicator.getLogger();
    }

    public String getAppId() {
        return this.appId;
    }
}
