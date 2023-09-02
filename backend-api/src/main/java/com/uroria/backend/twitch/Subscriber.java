package com.uroria.backend.twitch;

import com.uroria.backend.Backend;
import com.uroria.backend.BackendObject;
import com.uroria.base.utils.CollectionUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import lombok.NonNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Subscriber extends BackendObject<Subscriber> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private final ObjectList<Subscription> subscriptions;

    public Subscriber(@NonNull UUID uuid) {
        this.uuid = uuid;
        this.subscriptions = new ObjectArrayList<>();
    }


    public boolean isSubscriberOf(Streamer streamer) {
        if (streamer == null) return false;
        return isSubscriberOf(streamer.getStreamerUUID());
    }

    public boolean isSubscriberOf(UUID uuid) {
        if (uuid == null) return false;
        return this.subscriptions.stream().anyMatch(subscription -> subscription.getStreamerUUID().equals(uuid));
    }

    public Optional<Subscription> getSubscription(UUID streamerUUID) {
        if (streamerUUID == null) return Optional.empty();
        return this.subscriptions.stream().filter(sub -> sub.getStreamerUUID().equals(streamerUUID)).findAny();
    }

    public Optional<Subscription> getSubscription(Streamer streamer) {
        if (streamer == null) return Optional.empty();
        return this.subscriptions.stream().filter(sub -> sub.getStreamerUUID().equals(streamer.getStreamerUUID())).findAny();
    }

    public void addSubscription(@NonNull Subscription subscription) {
        this.subscriptions.add(subscription);
    }

    public void removeSubscription(@NonNull Subscription subscription) {
        this.subscriptions.remove(subscription);
    }

    public List<Subscription> getSubscriptions() {
        return Collections.unmodifiableList(this.subscriptions);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    @Override
    public void modify(Subscriber subscriber) {
        this.deleted = subscriber.deleted;
        CollectionUtils.overrideCollection(this.subscriptions, subscriber.subscriptions);
        this.subscriptions.removeIf(BackendObject::isDeleted);
        for (Subscription subscription : this.subscriptions) {
            Optional<Subscription> any = subscriber.subscriptions.stream().filter(sub -> sub.equals(subscription)).findAny();
            if (any.isEmpty()) continue;
            subscription.modify(any.get());
        }
    }

    @Override
    public void update() {
        Backend.getAPI().getTwitchManager().updateSubscriber(this);
    }

    @Override
    public String toString() {
        return "Subscriber{uuid="+this.uuid+"}";
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Subscriber subscriber)) return false;
        return this.uuid.equals(subscriber.uuid);
    }
}
