package com.uroria.backend.communication.request;

import com.rabbitmq.client.Channel;
import com.uroria.backend.communication.CommunicationPoint;
import com.uroria.backend.communication.Communicator;
import com.uroria.backend.communication.response.Response;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

public class RequestPoint extends CommunicationPoint {
    private final ObjectSet<Requester<?, ?>> requesters;

    public RequestPoint(Communicator communicator, String topic) {
        super(communicator, "request-" + topic);
        this.requesters = new ObjectArraySet<>();
    }

    public final <REQ extends Request, RES extends Response> Requester<REQ, RES> registerRequester(@NonNull Class<REQ> requestClass, @NonNull Class<RES> responseClass, String messageType) {
        Requester<REQ, RES> requester = new Requester<>(this, messageType, requestClass, responseClass);
        this.requesters.add(requester);
        return requester;
    }

    public final Requester<?, ?> getRequester(String messageType) {
        return this.requesters.stream()
                .filter(requester -> requester.getMessageType().equals(messageType))
                .findAny().orElse(null);
    }

    @SuppressWarnings("unchecked")
    public final <REQ extends Request, RES extends Response> Requester<REQ, RES> getRequester(@NonNull Class<REQ> requestClass, @NonNull Class<RES> responseClass) {
        return this.requesters.stream()
                .filter(requester -> requester.getRequestClass().equals(requestClass))
                .filter(requester -> requester.getResponseClass().equals(responseClass))
                .map(requester -> (Requester<REQ, RES>) requester)
                .findAny().orElse(null);
    }

    public final void unregisterRequester(@NonNull String messageType) {
        this.requesters.removeIf(requester -> requester.getMessageType().equals(messageType));
    }

    public final void unregisterRequester(@NonNull Class<? extends Request> requestClass, @NonNull Class<? extends Response> responseClass) {
        this.requesters.removeIf(requester -> requester.getRequestClass().equals(requestClass) &&
                requester.getResponseClass().equals(responseClass));
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
}
