package com.uroria.backend.impl.twitch.streamer;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.twitch.Streamer;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class StreamerChannelRequestChannel extends PulsarRequest<Streamer, String> {
    public StreamerChannelRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "twitch:streamer:request:channel", "twitch:streamer:response:channel", name, 3000);
    }
}
