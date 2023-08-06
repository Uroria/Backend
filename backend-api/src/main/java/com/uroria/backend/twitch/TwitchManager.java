package com.uroria.backend.twitch;

import lombok.NonNull;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface TwitchManager {

    default Optional<Streamer> getStreamer(UUID streamerUUID) {
        return getStreamer(streamerUUID, 3000);
    }

    default Optional<Streamer> getStreamer(String channelName) {
        return getStreamer(channelName, 3000);
    }

    default Optional<Subscriber> getSubscriber(UUID uuid) {
        return getSubscriber(uuid, 3000);
    }

    default Collection<Subscriber> getSubscribers(Streamer streamer) {
        return getSubscribers(streamer, 10000);
    }

    Optional<Streamer> getStreamer(UUID streamerUUID, int timeout);

    Optional<Streamer> getStreamer(String channelName, int timeout);

    Optional<Subscriber> getSubscriber(UUID uuid, int timeout);

    Collection<Subscriber> getSubscribers(Streamer streamer, int timeout);

    void updateSubscriber(@NonNull Subscriber subscriber);

    void updateStreamer(@NonNull Streamer streamer);
}
