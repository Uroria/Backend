package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PunishmentType;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

public final class BackendPunishment implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID affectedPlayer;
    private int punishmentType;
    private int reasonId;
    private long endDate;
    public BackendPunishment(UUID affectedPlayer) {
        this.affectedPlayer = affectedPlayer;
    }

    public void punish(PunishmentType punishmentType, int reasonId, long endDate) {
        this.punishmentType = punishmentType.getId();
        this.reasonId = reasonId;
        this.endDate = endDate;
    }

    public void unpunish() {
        this.punishmentType = 0;
        this.endDate = 0;
        this.reasonId = 0;
    }

    public boolean isMuted() {
        if (this.punishmentType == 0) return false;
        return getPunishmentType().map(punishmentType -> punishmentType == PunishmentType.PERMANENT_MUTE || punishmentType == PunishmentType.TEMPORARY_MUTE).orElse(false);
    }

    public boolean isBanned() {
        if (this.punishmentType == 0) return false;
        return !isMuted();
    }

    public UUID getAffectedPlayer() {
        return affectedPlayer;
    }

    public Optional<Integer> getReason() {
        if (reasonId == 0) return Optional.empty();
        return Optional.of(reasonId);
    }

    public Optional<Long> getEndDate() {
        if (endDate == 0) return Optional.empty();
        return Optional.of(this.endDate);
    }

    public Optional<PunishmentType> getPunishmentType() {
        if (this.punishmentType == 0) return Optional.empty();
        return Optional.ofNullable(PunishmentType.fromId(this.punishmentType));
    }
}
