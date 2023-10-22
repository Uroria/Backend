package com.uroria.backend.communication.request;

import com.google.gson.JsonElement;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.io.BackendInputStream;
import com.uroria.backend.communication.io.BackendOutputStream;
import com.uroria.backend.communication.response.Response;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public final class Requester<REQ extends Request, RES extends Response> {
    private final RequestPoint point;
    private final String messageType;
    private final Class<REQ> requestClass;
    private final Class<RES> responseClass;
    private final ObjectSet<String> openedRequests;

    Requester(RequestPoint point, String messageType, Class<REQ> requestClass, Class<RES> responseClass) {
        this.point = point;
        this.messageType = messageType;
        this.requestClass = requestClass;
        this.responseClass = responseClass;
        this.openedRequests = new ObjectArraySet<>();
    }
    
    public Result<RES> request(@NonNull REQ request, long timeoutMs) {
        try {
            JsonElement element = request.toElement();
            BackendOutputStream output = new BackendOutputStream();
            output.writeUTF(messageType);
            output.writeJsonElement(element);
            output.close();
            String correlationId = UUID.randomUUID().toString();
            AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                    .appId(this.point.getAppId())
                    .correlationId(correlationId)
                    .build();

            this.openedRequests.add(correlationId);
            this.point.getChannel().basicPublish("request", this.point.getQueue(), properties, output.toByteArray());

            BlockingQueue<byte[]> responseQueue = new ArrayBlockingQueue<>(1);

            Consumer consumer = new DefaultConsumer(point.getChannel()) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    long deliveryTag = envelope.getDeliveryTag();
                    String appId = properties.getAppId();
                    if (appId == null || !appId.equals(point.getAppId())) {
                        point.getChannel().basicNack(deliveryTag, false, true);
                        return;
                    }
                    String id = properties.getCorrelationId();
                    if (id == null || !id.equals(correlationId)) {
                        if (!openedRequests.contains(id)) {
                            point.getChannel().basicNack(deliveryTag, false, false);
                            return;
                        }
                        point.getChannel().basicNack(deliveryTag, false, true);
                        return;
                    }
                    point.getChannel().basicAck(deliveryTag, false);
                    responseQueue.add(body);
                }
            };
            this.point.getChannel().basicConsume("response-" + this.point.getTopic(), false, consumer);

            byte[] bytes = responseQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (bytes == null) {
                return Result.none();
            }

            BackendInputStream input = new BackendInputStream(bytes);
            JsonElement responseElement = input.readJsonElement();
            input.close();
            return Result.some(Communicator.getGson().fromJson(responseElement, getResponseClass()));
        } catch (Exception exception) {
            return Result.problem(Problem.error(exception));
        }
    }

    Class<REQ> getRequestClass() {
        return requestClass;
    }

    Class<RES> getResponseClass() {
        return responseClass;
    }

    String getMessageType() {
        return messageType;
    }
}
