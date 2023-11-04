package com.uroria.backend.communication.broadcast;

import com.google.gson.JsonElement;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.uroria.backend.communication.CommunicationPoint;
import com.uroria.backend.communication.CommunicationThread;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.io.BackendInputStream;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BroadcastPoint extends CommunicationPoint {
    private final ObjectSet<Broadcaster<?>> broadcasters;

    public BroadcastPoint(Communicator communicator, String topic) {
        super(communicator, "broadcast-" + topic);
        this.broadcasters = new ObjectArraySet<>();
        try {
            this.channel.exchangeDeclare(this.topic, BuiltinExchangeType.FANOUT);
            this.channel.queueBind(queue, this.topic, "ignored");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        BroadcastThread broadcastThread = new BroadcastThread(this);
        broadcastThread.start();
    }

    public final <T extends Broadcast> Broadcaster<T> registerBroadcaster(@NonNull Class<T> broadcastClass, String messageType) {
        Broadcaster<T> broadcaster = new Broadcaster<>(this, broadcastClass, messageType);
        this.broadcasters.add(broadcaster);
        return broadcaster;
    }

    public final Broadcaster<?> getBroadcaster(String messageType) {
        return this.broadcasters.stream()
                .filter(broadcaster -> broadcaster.getMessageType().equals(messageType))
                .findAny().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public final <T extends Broadcast> Broadcaster<T> getBroadcaster(@NonNull Class<T> broadcastClass) {
        return this.broadcasters.stream()
                .filter(broadcaster -> broadcaster.getBroadcastClass().hashCode() == broadcastClass.hashCode())
                .map(broadcaster -> (Broadcaster<T>) broadcaster)
                .findAny().orElse(null);
    }

    public final void unregisterBroadcaster(@NonNull String messageType) {
        this.broadcasters.removeIf(broadcaster -> broadcaster.getMessageType().equals(messageType));
    }

    public final void unregisterBroadcaster(@NonNull Class<? extends Broadcast> broadcastClass) {
        this.broadcasters.removeIf(broadcaster -> broadcaster.getBroadcastClass().equals(broadcastClass));
    }

    String getQueue() {
        return this.queue;
    }

    Channel getChannel() {
        return this.channel;
    }

    String getTopic() {
        return this.topic;
    }

    private static final class BroadcastThread extends CommunicationThread {
        private final BroadcastPoint point;

        BroadcastThread(BroadcastPoint point) {
            super(point);
            this.point = point;
        }

        @Override
        public void run() {
            while (point.channel.isOpen()) {
                Optional<byte[]> optionalBytes = awaitMessage();
                if (optionalBytes.isEmpty()) continue;
                byte[] bytes = optionalBytes.get();
                CompletableFuture.runAsync(() -> {
                    try {
                        BackendInputStream input = new BackendInputStream(bytes);
                        String messageType = input.readUTF();
                        JsonElement element = input.readJsonElement();
                        input.close();
                        receiveBroadcast(this.point.getBroadcaster(messageType), element);
                    } catch (Exception exception) {
                        point.logger().error("Cannot receive broadcast", exception);
                    }
                });
            }
        }

        private <T extends Broadcast> void receiveBroadcast(Broadcaster<T> broadcaster, JsonElement element) {
            if (broadcaster == null) return;
            T broadcast = Communicator.getGson().fromJson(element, broadcaster.getBroadcastClass());
            broadcaster.receiveBroadcast(broadcast);
        }
    }
}
