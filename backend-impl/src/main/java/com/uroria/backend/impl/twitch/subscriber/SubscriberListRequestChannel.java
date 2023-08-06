package com.uroria.backend.impl.twitch.subscriber;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.twitch.Subscriber;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.List;
import java.util.UUID;

public final class SubscriberListRequestChannel extends PulsarRequest<List<Subscriber>, UUID> {
    public SubscriberListRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "twitch:subscriber:request:all", "twitch:subscriber:response:all", name, 10000);
    }
}
