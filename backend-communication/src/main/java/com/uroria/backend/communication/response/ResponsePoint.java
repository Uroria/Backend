package com.uroria.backend.communication.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.uroria.backend.communication.CommunicationPoint;
import com.uroria.backend.communication.CommunicationThread;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.io.BackendInputStream;
import com.uroria.backend.communication.io.BackendOutputStream;
import com.uroria.backend.communication.request.Request;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class ResponsePoint extends CommunicationPoint {
    private final ObjectSet<Responser<?, ?>> responsers;

    public ResponsePoint(Communicator communicator, String topic) {
        super(communicator, "request-" + topic);
        this.responsers = new ObjectArraySet<>();
        try {
            this.channel.exchangeDeclare(this.topic, BuiltinExchangeType.FANOUT);
            this.channel.queueBind(queue, this.topic, "");
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        RequestThread thread = new RequestThread(this);
        thread.start();
    }

    public final <REQ extends Request, RES extends Response> Responser<REQ, RES> registerResponser(@NonNull Class<REQ> requestClass, @NonNull Class<RES> responseClass, String messageType, RequestListener<REQ, RES> listener) {
        Responser<REQ, RES> responser = new Responser<>(messageType, requestClass, responseClass, listener);
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
            point.logger().info("Initializing request-thread for " + point.topic);
            this.point = point;
        }

        @Override
        public void run() {
            point.logger().info("Starting request-thread for " + point.topic);
            while (point.channel.isOpen()) {
                if (!isAlive()) {
                    point.logger().info("request-thread of " + point.topic + " is dead");
                    return;
                }
                if (isInterrupted()) {
                    point.logger().info("request-thread of " + point.topic + " was interrupted");
                    return;
                }
                AMQP.BasicProperties properties;
                byte[] bytes;
                try {
                    GetResponse response = this.point.channel.basicGet(point.queue, true);
                    if (response == null) continue;
                    properties = response.getProps();
                    bytes = response.getBody();
                } catch (Exception exception) {
                    if (!point.channel.isOpen()) continue;
                    this.point.logger().error("Unable to consume message for topic " + point.topic, exception);
                    continue;
                }
                if (bytes == null || properties == null) {
                    continue;
                }
                CompletableFuture.runAsync(() -> {
                    //noinspection SpellCheckingInspection
                    try {
                        point.logger().info("Reading");
                        BackendInputStream input = new BackendInputStream(bytes);
                        String messageType = input.readUTF();
                        JsonElement element = input.readJsonElement();
                        input.close();

                        Response response = request(this.point.getResponser(messageType), element);
                        BackendOutputStream output = new BackendOutputStream();
                        if (response != null) output.writeJsonElement(response.toElement());
                        else output.writeJsonElement(JsonNull.INSTANCE);
                        output.close();

                        AMQP.BasicProperties responseProperties = new AMQP.BasicProperties.Builder()
                                .appId(properties.getAppId())
                                .correlationId(properties.getCorrelationId())
                                .build();
                        point.logger().info("Replying");
                        this.point.channel.basicPublish("", properties.getReplyTo(), responseProperties, output.toByteArray());
                    } catch (IOException ignored) {
                        /*
                            ███╗░░██╗██╗░█████╗░██╗░░██╗████████╗░██████╗
                            ████╗░██║██║██╔══██╗██║░░██║╚══██╔══╝██╔════╝
                            ██╔██╗██║██║██║░░╚═╝███████║░░░██║░░░╚█████╗░
                            ██║╚████║██║██║░░██╗██╔══██║░░░██║░░░░╚═══██╗
                            ██║░╚███║██║╚█████╔╝██║░░██║░░░██║░░░██████╔╝
                            ╚═╝░░╚══╝╚═╝░╚════╝░╚═╝░░╚═╝░░░╚═╝░░░╚═════╝░,
                            weil wir scheißen auf diesen fehler 😎
                         */
                    } catch (Exception exception) {
                        point.logger().error("Cannot response to request", exception);
                    }
                });
            }
            point.logger().info("Closed request-thread " + point.topic);
        }

        private <REQ extends Request, RES extends Response> RES request(Responser<REQ, RES> responser, JsonElement element) {
            if (responser == null) {
                this.point.logger().warn("Cannot find valid responser for");
                return null;
            }
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
