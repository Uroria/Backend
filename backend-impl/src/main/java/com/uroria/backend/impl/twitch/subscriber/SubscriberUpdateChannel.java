package com.uroria.backend.impl.twitch.subscriber;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.twitch.Subscriber;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class SubscriberUpdateChannel extends PulsarUpdate<Subscriber> {
    private final Consumer<Subscriber> subscriberConsumer;

    public SubscriberUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<Subscriber> subscriberConsumer) throws PulsarClientException {
        super(pulsarClient, "twitch:subscriber:update", name);
        this.subscriberConsumer = subscriberConsumer;
    }

    @Override
    protected void onUpdate(Subscriber subscriber) {
        this.subscriberConsumer.accept(subscriber);
    }
}
