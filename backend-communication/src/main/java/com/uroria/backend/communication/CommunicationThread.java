package com.uroria.backend.communication;

import com.rabbitmq.client.GetResponse;

import java.util.Optional;

public abstract class CommunicationThread extends Thread {
    protected final CommunicationPoint point;

    public CommunicationThread(CommunicationPoint point) {
        this.point = point;
    }

    protected Optional<byte[]> awaitMessage() {
        try {
            GetResponse response = this.point.channel.basicGet(point.queue, true);
            if (response == null) return Optional.empty();
            if (response.getProps().getAppId().equals(this.point.getAppId())) return Optional.empty();
            return Optional.of(response.getBody());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
