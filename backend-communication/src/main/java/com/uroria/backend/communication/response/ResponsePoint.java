package com.uroria.backend.communication.response;

import com.google.gson.JsonElement;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.uroria.backend.communication.CommunicationPoint;
import com.uroria.backend.communication.CommunicationThread;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.io.BackendInputStream;
import com.uroria.backend.communication.io.BackendOutputStream;
import com.uroria.backend.communication.request.Request;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ResponsePoint extends CommunicationPoint {
    private final ObjectSet<Responser<?, ?>> responsers;

    public ResponsePoint(Communicator communicator, String topic) {
        super(communicator, "response-" + topic);
        this.responsers = new ObjectArraySet<>();
    }

    public final <REQ extends Request, RES extends Response> Responser<REQ, RES> registerResponser(@NonNull Class<REQ> requestClass, @NonNull Class<RES> responseClass, String messageType, RequestListener<REQ, RES> listener) {
        Responser<REQ, RES> responser = new Responser<>(this, messageType, requestClass, responseClass, listener);
        this.responsers.add(responser);
        return responser;
    }

    public final Responser<?, ?> getResponser(String messageType) {
        return this.responsers.stream()
                .filter(responser -> responser.getMessageType().equals(messageType))
                .findAny().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public final <REQ extends Request, RES extends Response> Responser<REQ, RES> getResponser(Class<REQ> requestClass, Class<RES> responseClass) {
        return this.responsers.stream()
                .filter(responser -> responser.getResponseClass().equals(responseClass))
                .filter(responser -> responser.getRequestClass().equals(requestClass))
                .map(responser -> (Responser<REQ, RES>) responser)
                .findAny().orElse(null);
    }

    String getQueue() {
        return queue;
    }

    Channel getChannel() {
        return channel;
    }

    String getTopic() {
        return topic;
    }
    
    private static final class RequestThread extends CommunicationThread {
        private final ResponsePoint point;

        RequestThread(ResponsePoint point) {
            super(point);
            this.point = point;
        }

        @Override
        public void run() {
            while (point.channel.isOpen()) {
                BlockingQueue<byte[]> byteQueue = new ArrayBlockingQueue<>(1);
                BlockingQueue<AMQP.BasicProperties> propertiesQueue = new ArrayBlockingQueue<>(1);
                DeliverCallback callback = (consumerTag, message) -> {
                    AMQP.BasicProperties properties = message.getProperties();
                    if (properties.getAppId().equals(this.point.getAppId())) return;
                    propertiesQueue.add(properties);
                    byteQueue.add(message.getBody());
                };
                Optional<byte[]> optionalBytes;
                AMQP.BasicProperties properties;
                try {
                    this.point.channel.basicConsume("request-" + point.topic, true, callback, (consumerTag -> {}));
                    optionalBytes = Optional.ofNullable(byteQueue.poll(5, TimeUnit.SECONDS));
                    properties = propertiesQueue.poll(5, TimeUnit.SECONDS);
                } catch (Exception exception) {
                    continue;
                }
                if (optionalBytes.isEmpty()) continue;
                if (properties == null) continue;
                byte[] bytes = optionalBytes.get();
                CompletableFuture.runAsync(() -> {
                    try {
                        BackendInputStream input = new BackendInputStream(bytes);
                        String messageType = input.readUTF();
                        JsonElement element = input.readJsonElement();
                        input.close();

                        Response response = request(this.point.getResponser(messageType), element);
                        BackendOutputStream output = new BackendOutputStream();
                        output.writeJsonElement(response.toElement());
                        output.close();

                        AMQP.BasicProperties responseProperties = new AMQP.BasicProperties.Builder()
                                .appId(properties.getAppId())
                                .correlationId(properties.getCorrelationId())
                                .build();
                        this.point.channel.basicPublish("", point.queue, responseProperties, output.toByteArray());
                    } catch (Exception exception) {
                        point.logger().error("Cannot response to request", exception);
                    }
                });
            }
        }

        private <REQ extends Request, RES extends Response> RES request(Responser<REQ, RES> responser, JsonElement element) {
            if (responser == null) return null;
            REQ request = Communicator.getGson().fromJson(element, responser.getRequestClass());
            Optional<RES> optionalResponse = responser.getListener().onRequest(request);
            if (optionalResponse.isPresent()) {
                point.logger().info("Request of " + responser.getMessageType() + " returned valid");
                return optionalResponse.get();
            }
            point.logger().info("Request of " + responser.getMessageType() + " returned null");
            return null;
        }
    }
}
