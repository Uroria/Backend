package com.uroria.backend.impl.twitch.streamer;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.twitch.Streamer;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class StreamerUpdateChannel extends PulsarUpdate<Streamer> {
    private final Consumer<Streamer> streamerConsumer;

    public StreamerUpdateChannel(@NonNull PulsarClient pulsarClient, @NonNull String name, Consumer<Streamer> streamerConsumer) throws PulsarClientException {
        super(pulsarClient, "twitch:streamer:update", name);
        this.streamerConsumer = streamerConsumer;
    }

    @Override
    protected void onUpdate(Streamer streamer) {
        this.streamerConsumer.accept(streamer);
    }
}
