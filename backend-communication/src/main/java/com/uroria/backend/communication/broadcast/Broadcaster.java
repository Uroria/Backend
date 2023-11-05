package com.uroria.backend.communication.broadcast;

import com.google.gson.JsonElement;
import com.rabbitmq.client.AMQP;
import com.uroria.backend.communication.io.BackendOutputStream;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

public final class Broadcaster<T extends Broadcast> {
    private final BroadcastPoint point;
    private final String messageType;
    private final ObjectSet<BroadcastListener<T>> listeners;
    private final Class<T> broadcastClass;

    Broadcaster(BroadcastPoint point, Class<T> broadcastClass, String messageType) {
        this.point = point;
        this.messageType = messageType;
        this.broadcastClass = broadcastClass;
        this.listeners = new ObjectArraySet<>();
    }

    public void registerListener(BroadcastListener<T> listener) {
        this.listeners.add(listener);
    }

    public void unregisterListeners() {
        this.listeners.clear();
    }

    public Result<Void> broadcast(@NonNull T broadcast) {
        try {
            JsonElement element = broadcast.toElement();
            BackendOutputStream output = new BackendOutputStream();
            output.writeUTF(messageType);
            output.writeJsonElement(element);
            output.close();
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .appId(this.point.getAppId())
                    .build();
            this.point.getChannel().basicPublish(this.point.getTopic(), "ignored", properties, output.toByteArray());
            return Result.none();
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    void receiveBroadcast(T broadcast) {
        for (BroadcastListener<T> listener : this.listeners) {
            listener.onBroadcast(broadcast);
        }
    }

    Class<T> getBroadcastClass() {
        return broadcastClass;
    }

    String getMessageType() {
        return messageType;
    }
}
