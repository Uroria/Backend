package com.uroria.backend.communication;

import com.rabbitmq.client.DeliverCallback;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class CommunicationThread extends Thread {
    protected final CommunicationPoint point;

    public CommunicationThread(CommunicationPoint point) {
        this.point = point;
    }

    protected Optional<byte[]> awaitMessage() {
        BlockingQueue<byte[]> byteQueue = new ArrayBlockingQueue<>(1);
        DeliverCallback callback = (consumerTag, message) -> {
            if (message.getProperties().getAppId().equals(this.point.getAppId())) return;
            byteQueue.add(message.getBody());
        };
        try {
            this.point.channel.basicConsume(point.queue, true, callback, (consumerTag -> {}));
            return Optional.ofNullable(byteQueue.poll(5, TimeUnit.SECONDS));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
