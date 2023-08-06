package com.uroria.backend.bukkit.twitch;

import com.uroria.backend.bukkit.BackendBukkitPlugin;
import com.uroria.backend.bukkit.utils.BukkitUtils;
import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.backend.impl.twitch.AbstractTwitchManager;
import com.uroria.backend.impl.twitch.streamer.StreamerChannelRequestChannel;
import com.uroria.backend.impl.twitch.streamer.StreamerUUIDRequestChannel;
import com.uroria.backend.impl.twitch.streamer.StreamerUpdateChannel;
import com.uroria.backend.impl.twitch.subscriber.SubscriberListRequestChannel;
import com.uroria.backend.impl.twitch.subscriber.SubscriberUUIDRequestChannel;
import com.uroria.backend.impl.twitch.subscriber.SubscriberUpdateChannel;
import com.uroria.backend.twitch.Streamer;
import com.uroria.backend.twitch.Subscriber;
import com.uroria.backend.twitch.TwitchManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class TwitchManagerImpl extends AbstractTwitchManager implements TwitchManager {

    private StreamerUUIDRequestChannel streamerUUIDRequest;
    private StreamerChannelRequestChannel streamerChannelRequest;
    private StreamerUpdateChannel streamerUpdate;
    private SubscriberListRequestChannel subscriberListRequest;
    private SubscriberUUIDRequestChannel subscriberUUIDRequest;
    private SubscriberUpdateChannel subscriberUpdate;

    public TwitchManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
    }

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.streamerUUIDRequest = new StreamerUUIDRequestChannel(this.pulsarClient, identifier);
        this.streamerChannelRequest = new StreamerChannelRequestChannel(this.pulsarClient, identifier);
        this.streamerUpdate = new StreamerUpdateChannel(this.pulsarClient, identifier, this::checkStreamer);
        this.subscriberListRequest = new SubscriberListRequestChannel(this.pulsarClient, identifier);
        this.subscriberUUIDRequest = new SubscriberUUIDRequestChannel(this.pulsarClient, identifier);
        this.subscriberUpdate = new SubscriberUpdateChannel(this.pulsarClient, identifier, this::checkSubscriber);
        runCacheChecker();
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.streamerUUIDRequest != null) this.streamerUUIDRequest.close();
        if (this.streamerChannelRequest != null) this.streamerChannelRequest.close();
        if (this.streamerUpdate != null) this.streamerUpdate.close();
        if (this.subscriberListRequest != null) this.subscriberListRequest.close();
        if (this.subscriberUUIDRequest != null) this.subscriberUUIDRequest.close();
        if (this.subscriberUpdate != null) this.subscriberUpdate.close();
    }

    @Override
    protected void checkSubscriber(@NonNull Subscriber subscriber) {
        if (this.subscribers.stream().noneMatch(subscriber::equals)) return;

        for (Subscriber cachedSubscriber : this.subscribers) {
            if (!cachedSubscriber.equals(subscriber)) continue;
            cachedSubscriber.modify(subscriber);

            logger.info("Updated " + subscriber);
            BukkitUtils.callAsyncEvent(new SubscriberUpdateEvent(cachedSubscriber));
            return;
        }

        logger.info("Adding " + subscriber);
        this.subscribers.add(subscriber);
        BukkitUtils.callAsyncEvent(new SubscriberUpdateEvent(subscriber));
    }

    @Override
    protected void checkStreamer(@NonNull Streamer streamer) {
        if (this.streamers.stream().noneMatch(streamer::equals)) return;

        for (Streamer cachedStreamer : this.streamers) {
            if (!cachedStreamer.equals(streamer)) continue;
            cachedStreamer.modify(streamer);

            logger.info("Updated " + streamer);
            BukkitUtils.callAsyncEvent(new StreamerUpdateEvent(cachedStreamer));
            return;
        }

        logger.info("Adding " + streamer);
        this.streamers.add(streamer);
        BukkitUtils.callAsyncEvent(new StreamerUpdateEvent(streamer));
    }

    @Override
    public Optional<Streamer> getStreamer(UUID streamerUUID, int timeout) {
        for (Streamer streamer : this.streamers) {
            if (streamer.getStreamerUUID().equals(streamerUUID)) return Optional.of(streamer);
        }

        if (BackendBukkitPlugin.isOffline()) return Optional.empty();

        Optional<Streamer> request = streamerUUIDRequest.request(streamerUUID, timeout);
        request.ifPresent(this.streamers::add);
        return request;
    }

    @Override
    public Optional<Streamer> getStreamer(String channelName, int timeout) {
        for (Streamer streamer : this.streamers) {
            if (streamer.getTwitchChannel().equals(channelName)) return Optional.of(streamer);
        }

        if (BackendBukkitPlugin.isOffline()) return Optional.empty();

        Optional<Streamer> request = streamerChannelRequest.request(channelName, timeout);
        request.ifPresent(this.streamers::add);
        return request;
    }

    @Override
    public Optional<Subscriber> getSubscriber(UUID uuid, int timeout) {
        for (Subscriber subscriber : this.subscribers) {
            if (subscriber.getUUID().equals(uuid)) return Optional.of(subscriber);
        }

        if (BackendBukkitPlugin.isOffline()) return Optional.empty();

        Optional<Subscriber> request = subscriberUUIDRequest.request(uuid, timeout);
        request.ifPresent(this.subscribers::add);
        return request;
    }

    @Override
    public Collection<Subscriber> getSubscribers(Streamer streamer, int timeout) {
        Collection<Subscriber> subscribers = new ObjectArraySet<>();

        if (!BackendBukkitPlugin.isOffline()) {
            Optional<List<Subscriber>> request = this.subscriberListRequest.request(streamer.getStreamerUUID(), timeout);
            request.ifPresent(subscribers::addAll);
        }

        return subscribers;
    }

    @Override
    public void updateSubscriber(@NonNull Subscriber subscriber) {
        try {
            checkSubscriber(subscriber);
            if (BackendBukkitPlugin.isOffline()) return;
            this.subscriberUpdate.update(subscriber);
        } catch (Exception exception) {
            logger.error("Cannot update " + subscriber);
        }
    }

    @Override
    public void updateStreamer(@NonNull Streamer streamer) {
        try {
            checkStreamer(streamer);
            if (BackendBukkitPlugin.isOffline()) return;
            this.streamerUpdate.update(streamer);
        } catch (Exception exception) {
            logger.error("Cannot update " + streamer);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            ObjectArraySet<UUID> markedForRemoval = new ObjectArraySet<>();
            for (Subscriber subscriber : this.subscribers) {
                UUID uuid = subscriber.getUUID();
                if (Bukkit.getPlayer(uuid) == null) markedForRemoval.add(uuid);
            }
            for (Streamer streamer : this.streamers) {
                UUID uuid = streamer.getStreamerUUID();
                if (Bukkit.getPlayer(uuid) == null) markedForRemoval.add(uuid);
            }
            return markedForRemoval;
        }, 30, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.streamers.removeIf(streamer -> streamer.getStreamerUUID().equals(uuid));
                this.subscribers.removeIf(subscriber -> subscriber.getUUID().equals(uuid));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " Streamers and Subscribers flushed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception in cache checker", throwable);
            runCacheChecker();
        });
    }
}
