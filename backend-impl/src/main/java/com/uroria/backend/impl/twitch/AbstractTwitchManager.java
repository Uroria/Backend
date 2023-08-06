package com.uroria.backend.impl.twitch;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.twitch.Streamer;
import com.uroria.backend.twitch.Subscriber;
import com.uroria.backend.twitch.TwitchManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractTwitchManager extends AbstractManager implements TwitchManager {
    protected final ObjectArraySet<Subscriber> subscribers;
    protected final ObjectArraySet<Streamer> streamers;

    public AbstractTwitchManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.subscribers = new ObjectArraySet<>();
        this.streamers = new ObjectArraySet<>();
    }

    abstract protected void checkSubscriber(@NonNull Subscriber subscriber);

    abstract protected void checkStreamer(@NonNull Streamer streamer);
}
