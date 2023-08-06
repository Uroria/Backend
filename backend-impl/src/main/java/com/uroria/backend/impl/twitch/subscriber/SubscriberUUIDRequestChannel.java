package com.uroria.backend.impl.twitch.subscriber;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.twitch.Subscriber;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class SubscriberUUIDRequestChannel extends PulsarRequest<Subscriber, UUID> {
    public SubscriberUUIDRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "twitch:subscriber:request:uuid", "twitch:subscriber:response:uuid", name, 3000);
    }
}
