package com.uroria.backend.impl.communication.broadcast;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.uroria.are.Application;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RabbitUpdateChannel implements UpdateChannel {

    private final String topic;
    private final Channel channel;
    private final String identifier;
    private final String updateQueue;

    public RabbitUpdateChannel(@NonNull Connection connection, String topic) throws RuntimeException {
        this.topic = topic;
        this.identifier = UUID.randomUUID().toString();
        try {
            if (Application.isOffline() || Application.isTest()) {
                this.channel = null;
                this.updateQueue = null;
                return;
            }
            this.channel = connection.createChannel();
            this.channel.exchangeDeclare("update", BuiltinExchangeType.DIRECT);
            this.updateQueue = this.channel.queueDeclare("update:" + topic, false, false, true, Map.of()).getQueue();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public final Result<Void> updateSync(byte @NonNull [] data) {
        try {
            if (this.channel == null || this.updateQueue == null) {
                return Result.none();
            }
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .appId(this.identifier)
                    .build();
            this.channel.basicPublish("update", this.updateQueue, properties, data);
            return Result.none();
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public final Result<Void> updateSync(@NonNull Supplier<byte[]> data) {
        return updateSync(data.get());
    }

    @Override
    public final CompletableFuture<Result<Void>> updateAsync(byte @NonNull [] data) {
        return CompletableFuture.supplyAsync(() -> updateSync(data));
    }

    @Override
    public final CompletableFuture<Result<Void>> updateAsync(@NonNull Supplier<byte[]> data) {
        return CompletableFuture.supplyAsync(() -> updateSync(data));
    }

    @Override
    public final Result<byte[]> awaitUpdate() {
        if (this.channel == null) {
            return Result.none();
        }
        BlockingQueue<byte[]> updateQueue = new ArrayBlockingQueue<>(1);
        DeliverCallback callback = (consumerTag, message) -> {
            if (message.getProperties().getAppId().equals(this.identifier)) return;
            updateQueue.add(message.getBody());
        };
        try {
            this.channel.basicConsume(this.updateQueue, true, callback, (consumerTag -> {}));
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
        try {
            return Result.of(updateQueue.take());
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public final Result<byte[]> awaitUpdate(int timeoutMs) {
        if (this.channel == null) {
            return Result.none();
        }
        BlockingQueue<byte[]> updateQueue = new ArrayBlockingQueue<>(1);
        DeliverCallback callback = (consumerTag, message) -> {
            if (message.getProperties().getAppId().equals(this.identifier)) return;
            updateQueue.add(message.getBody());
        };
        try {
            this.channel.basicConsume(this.updateQueue, true, callback, (consumerTag -> {}));
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
        try {
            return Result.of(updateQueue.poll(timeoutMs, TimeUnit.MILLISECONDS));
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public final String getTopic() {
        return this.topic;
    }

    @Override
    public void close() {
        try {
            if (this.channel != null) this.channel.close();
        } catch (Exception ignored) {}
    }
}
