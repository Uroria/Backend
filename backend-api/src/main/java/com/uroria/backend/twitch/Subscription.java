package com.uroria.backend.twitch;

import com.uroria.backend.Backend;
import com.uroria.backend.BackendObject;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public final class Subscription extends BackendObject<Subscription> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID streamerUuid;
    private int month;

    public Subscription(@NonNull UUID streamerUuid, int month) {
        this.streamerUuid = streamerUuid;
        this.month = month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getMonth() {
        return this.month;
    }

    /**
     * Gets the streamer or else null, if the Streamer could not be found.
     * If null this object gets automatically deleted
     */
    public @Nullable Streamer getStreamer() {
        if (deleted) return null;
        Optional<Streamer> streamer = Backend.getAPI().getTwitchManager().getStreamer(this.streamerUuid);
        if (streamer.isEmpty()) {
            delete();
            return null;
        }
        return streamer.get();
    }

    public @NotNull UUID getStreamerUUID() {
        return this.streamerUuid;
    }

    @Override
    public void modify(Subscription subscription) {
        this.deleted = subscription.deleted;
        this.month = subscription.month;
    }

    /**
     * Does literally nothing.
     */
    @Deprecated
    @Override
    public void update() {

    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Subscription subscription)) return false;
        return streamerUuid.equals(subscription.streamerUuid);
    }

    @Override
    public String toString() {
        return "Subscription{uuid="+this.streamerUuid+", month="+this.month+"}";
    }
}
