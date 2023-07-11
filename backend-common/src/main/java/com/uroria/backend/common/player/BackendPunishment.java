package com.uroria.backend.common.player;

import com.uroria.backend.common.helpers.PunishmentType;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;

public final class BackendPunishment implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final String displayReason;
    private final int reasonId;
    private final int punishmentType;
    private final long startMs;
    private final long durationInMs;

    public BackendPunishment(@NonNull Component displayReason, int reasonId, @NonNull PunishmentType punishmentType, Duration duration, long startMs) {
        if (startMs == 0) throw new NullPointerException("Start ms cannot be null");
        this.displayReason = MiniMessage.miniMessage().serialize(displayReason);
        this.reasonId = reasonId;
        this.punishmentType = punishmentType.getId();
        this.startMs = startMs;
        this.durationInMs = duration.toMillis();
    }

    public BackendPunishment(int reasonId, @NonNull Component displayReason, @NonNull PunishmentType type, Duration duration) {
        this.reasonId = reasonId;
        this.displayReason = MiniMessage.miniMessage().serialize(displayReason);
        this.punishmentType = type.getId();
        if (type.isPermanent()) {
            this.startMs = 0;
            this.durationInMs = 0;
            return;
        }
        this.durationInMs = duration.toMillis();
        this.startMs = System.currentTimeMillis();
    }

    public BackendPunishment(int reasonId, @NonNull Component displayReason, @NonNull PunishmentType type) {
        if (!type.isPermanent()) throw new NullPointerException("Missing duration because PunishmentType is missing");
        this.reasonId = reasonId;
        this.displayReason = MiniMessage.miniMessage().serialize(displayReason);
        this.punishmentType = type.getId();
        this.startMs = 0;
        this.durationInMs = 0;
    }

    boolean isOutdated() {
        if (getPunishmentType().isPermanent()) return false;
        return getEndMs() < System.currentTimeMillis();
    }

    public Component getDisplayReason() {
        return MiniMessage.miniMessage().deserialize(displayReason);
    }

    public int getReasonId() {
        return reasonId;
    }

    public PunishmentType getPunishmentType() {
        return PunishmentType.fromId(this.punishmentType);
    }

    /**
     * The duration in ms
     */
    public long getDuration() {
        return durationInMs;
    }

    /**
     * The start date in ms
     */
    public long getStart() {
        return startMs;
    }

    private long getEndMs() {
        return this.startMs + this.durationInMs;
    }
}
