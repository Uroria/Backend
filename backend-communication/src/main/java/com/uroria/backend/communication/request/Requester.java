package com.uroria.backend.communication.request;

import com.google.gson.JsonElement;
import com.rabbitmq.client.*;
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
    private final Channel channel;

    Requester(RequestPoint point, String messageType, Class<REQ> requestClass, Class<RES> responseClass) {
        this.point = point;
        this.messageType = messageType;
        this.requestClass = requestClass;
        this.responseClass = responseClass;
        this.openedRequests = new ObjectArraySet<>();
        this.channel = point.getChannel();
    }
    
    public Result<RES> request(@NonNull REQ request, long timeoutMs) {
        point.logger().info("Requesting on topic " + messageType + " with timeout " + timeoutMs);
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
                    .replyTo(this.point.getQueue())
                    .build();

            this.openedRequests.add(correlationId);
            this.channel.basicPublish(point.getTopic(), "ignored", properties, output.toByteArray());

            long start = System.currentTimeMillis();
            while (true) {
                if ((System.currentTimeMillis() - start) > timeoutMs) return Result.none();
                GetResponse response = this.channel.basicGet(this.point.getQueue(), false);
                if (response == null) continue;
                long deliveryTag = response.getEnvelope().getDeliveryTag();
                AMQP.BasicProperties responseProps = response.getProps();
                String appId = responseProps.getAppId();
                if (appId == null || !appId.equals(point.getAppId())) {
                    this.channel.basicNack(deliveryTag, false, true);
                    continue;
                }
                String id = responseProps.getCorrelationId();
                if (id == null || !id.equals(correlationId)) {
                    if (!openedRequests.contains(id)) {
                        this.channel.basicNack(deliveryTag, false, false);
                        continue;
                    }
                    this.channel.basicNack(deliveryTag, false, true);
                    continue;
                }
                this.channel.basicAck(deliveryTag, false);
                byte[] bytes = response.getBody();

                BackendInputStream input = new BackendInputStream(bytes);
                JsonElement responseElement = input.readJsonElement();
                input.close();
                if (responseElement.isJsonNull()) return Result.none();
                return Result.some(Communicator.getGson().fromJson(responseElement, getResponseClass()));
            }
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
