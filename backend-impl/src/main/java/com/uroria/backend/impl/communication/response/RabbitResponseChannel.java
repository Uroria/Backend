package com.uroria.backend.impl.communication.response;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RabbitResponseChannel implements ResponseChannel {

    private final String topic;
    private final Channel channel;
    private final String requestQueue;

    public RabbitResponseChannel(@NonNull Connection connection, String topic) throws RuntimeException {
        this.topic = topic;
        try {
            this.channel = connection.createChannel();
            channel.exchangeDeclare("response", BuiltinExchangeType.TOPIC);
            this.requestQueue = channel.queueDeclare("request:" + topic, false, false, true, Map.of()).getQueue();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public final Result<Request> awaitRequest() {
        try {
            BlockingQueue<byte[]> dataQueue = new ArrayBlockingQueue<>(1);
            BlockingQueue<String> correlationIdQueue = new ArrayBlockingQueue<>(1);
            BlockingQueue<String> replyToQueue = new ArrayBlockingQueue<>(1);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if (!dataQueue.offer(delivery.getBody())) return;
                AMQP.BasicProperties properties = delivery.getProperties();
                if (!correlationIdQueue.offer(properties.getCorrelationId())) return;
                replyToQueue.add(properties.getReplyTo());
            };

            channel.basicConsume(requestQueue, true, deliverCallback, consumerTag -> {});

            byte[] data = dataQueue.take();
            String correlationId = correlationIdQueue.take();
            String replyTo = replyToQueue.take();

            return Result.some(new Request() {
                @Override
                public byte[] getData() {
                    return data;
                }

                @Override
                public void respondSync(byte @NonNull [] data) {
                    try {
                        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                                .correlationId(correlationId)
                                .build();
                        channel.basicPublish("server:" + topic, replyTo, properties, data);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                }

                @Override
                public void respondSync(@NonNull Supplier<byte @NonNull []> data) {
                    respondSync(data.get());
                }

                @Override
                public void respondAsync(byte @NonNull [] data) {
                    CompletableFuture.runAsync(() -> respondSync(data));
                }

                @Override
                public void respondAsync(@NonNull Supplier<byte @NonNull []> data) {
                    CompletableFuture.runAsync(() -> respondSync(data));
                }

                @Override
                public String getTopic() {
                    return topic;
                }
            });
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public final Result<Request> awaitRequest(int timeoutMs) {
        try {
            BlockingQueue<byte[]> dataQueue = new ArrayBlockingQueue<>(1);
            BlockingQueue<String> correlationIdQueue = new ArrayBlockingQueue<>(1);
            BlockingQueue<String> replyToQueue = new ArrayBlockingQueue<>(1);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                if (!dataQueue.offer(delivery.getBody())) return;
                AMQP.BasicProperties properties = delivery.getProperties();
                if (!correlationIdQueue.offer(properties.getCorrelationId())) return;
                if (!replyToQueue.offer(properties.getReplyTo())) return;
            };

            channel.basicConsume(requestQueue, true, deliverCallback, consumerTag -> {});

            timeoutMs = timeoutMs / 3;

            byte[] data = dataQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            String correlationId = correlationIdQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            String replyTo = replyToQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);

            if (data == null || correlationId == null || replyTo == null) return Result.none();

            return Result.some(new Request() {
                @Override
                public byte[] getData() {
                    return data;
                }

                @Override
                public void respondSync(byte @NonNull [] data) {
                    try {
                        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                                .correlationId(correlationId)
                                .build();
                        channel.basicPublish("server:" + topic, replyTo, properties, data);
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                }

                @Override
                public void respondSync(@NonNull Supplier<byte @NonNull []> data) {
                    respondSync(data.get());
                }

                @Override
                public void respondAsync(byte @NonNull [] data) {
                    CompletableFuture.runAsync(() -> respondSync(data));
                }

                @Override
                public void respondAsync(@NonNull Supplier<byte @NonNull []> data) {
                    CompletableFuture.runAsync(() -> respondSync(data));
                }

                @Override
                public String getTopic() {
                    return topic;
                }
            });
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public final String getTopic() {
        return this.topic;
    }

    @Override
    public void close() throws IOException {
        try {
            this.channel.close();
        } catch (Exception ignored) {}
    }
}
