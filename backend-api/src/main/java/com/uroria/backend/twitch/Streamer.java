package com.uroria.backend.twitch;

import com.uroria.backend.Backend;
import com.uroria.backend.BackendObject;
import com.uroria.backend.user.User;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public final class Streamer extends BackendObject<Streamer> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID streamerUuid;
    private String twitchChannel;
    private String badge;

    public Streamer(@NonNull User streamer, @NonNull String twitchChannel) {
        this.streamerUuid = streamer.getUUID();
        this.twitchChannel = twitchChannel;
        this.badge = "";
    }

    /**
     * Gets the streamer or else null, if the Streamer could not be found.
     */
    public @Nullable User getStreamer() {
        if (deleted) return null;
        Optional<User> user = Backend.getAPI().getUserManager().getUser(this.streamerUuid);
        if (user.isEmpty()) {
            delete();
            return null;
        }
        return user.get();
    }

    public @NotNull UUID getStreamerUUID() {
        return this.streamerUuid;
    }

    public String getBadge() {
        return this.badge;
    }

    public void setBadge(@NonNull String badge) {
        this.badge = badge;
    }

    public String getTwitchChannel() {
        return this.twitchChannel;
    }

    public void setTwitchChannel(@NonNull String channel) {
        this.twitchChannel = channel;
    }

    /**
     * Deletes this object instantly. No updating is required because it's done automatically.
     */
    @Override
    public void delete() {
        super.delete();
        if (deleted) return;
        Backend.getAPI().getTwitchManager().getSubscribers(this).forEach(subscriber -> {
            subscriber.getSubscription(this).ifPresent(subscription -> {
                subscriber.removeSubscription(subscription);
                subscriber.update();
            });
        });
    }

    @Override
    public void update() {
        Backend.getAPI().getTwitchManager().updateStreamer(this);
    }

    @Override
    public void modify(Streamer streamer) {
        this.deleted = streamer.deleted;
        this.twitchChannel = streamer.twitchChannel;
        this.badge = streamer.badge;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Streamer streamer)) return false;
        return this.streamerUuid.equals(streamer.streamerUuid);
    }

    @Override
    public String toString() {
        return "Streamer{uuid="+this.streamerUuid+", channel="+this.twitchChannel+"}";
    }
}
