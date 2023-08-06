package com.uroria.backend.impl.twitch.streamer;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.twitch.Streamer;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class StreamerUUIDRequestChannel extends PulsarRequest<Streamer, UUID> {
    public StreamerUUIDRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "twitch:streamer:request:uuid", "twitch:streamer:response:uuid", name, 3000);
    }
}
