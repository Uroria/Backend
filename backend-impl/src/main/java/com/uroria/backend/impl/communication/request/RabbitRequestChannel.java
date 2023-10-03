package com.uroria.backend.impl.communication.request;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RabbitRequestChannel implements RequestChannel {

    private final String topic;
    private final Channel channel;
    private final String requestQueue;
    private final String replyQueue;
    private final ObjectSet<String> openedRequests;

    public RabbitRequestChannel(@NonNull Connection connection, String topic) throws RuntimeException {
        this.topic = topic;
        try {
            this.channel = connection.createChannel();
            this.channel.exchangeDeclare("request", BuiltinExchangeType.TOPIC);
            this.requestQueue = this.channel.queueDeclare("request:" + topic, false, false, true, Map.of()).getQueue();
            this.replyQueue = this.channel.queueDeclare().getQueue();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        this.openedRequests = new ObjectArraySet<>();
    }

    @Override
    public final Result<byte[]> requestSync(byte @NonNull [] data, int timeout) {
        try {
            String correlationId = UUID.randomUUID().toString();

            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .correlationId(correlationId)
                    .replyTo(replyQueue)
                    .build();

            this.openedRequests.add(correlationId);
            channel.basicPublish("client:" + this.topic, this.requestQueue, properties, data);

            BlockingQueue<byte[]> responseQueue = new ArrayBlockingQueue<>(1);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();
                    String id = properties.getCorrelationId();
                    if (!id.equals(correlationId)) {
                        if (!openedRequests.contains(id)) {
                            channel.basicNack(deliveryTag, false, false);
                            return;
                        }
                        channel.basicNack(deliveryTag, false, true);
                        return;
                    }
                    channel.basicAck(deliveryTag, false);
                    responseQueue.add(body);
                    openedRequests.remove(correlationId);
                }
            };

            channel.basicConsume(replyQueue, false, consumer);

            byte[] bytes = responseQueue.poll(timeout, TimeUnit.MILLISECONDS);
            if (bytes == null) {
                this.openedRequests.remove(correlationId);
                return Result.none();
            }
            return Result.of(bytes);
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public final Result<byte[]> requestSync(@NonNull Supplier<byte @NonNull []> data, int timeout) {
        return requestSync(data.get(), timeout);
    }

    @Override
    public final CompletableFuture<Result<byte[]>> requestAsync(byte @NonNull [] data, int timeout) {
        return CompletableFuture.supplyAsync(() -> requestSync(data, timeout));
    }

    @Override
    public final CompletableFuture<Result<byte[]>> requestAsync(@NonNull Supplier<byte @NonNull []> data, int timeout) {
        return CompletableFuture.supplyAsync(() -> requestSync(data, timeout));
    }

    @Override
    public final String getTopic() {
        return this.topic;
    }

    @Override
    public void close() {
        try {
            this.channel.close();
        } catch (Exception ignored) {}
    }
}
