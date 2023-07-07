package com.uroria.backend.common.helpers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
public enum PunishmentType {
    PERMANENT_BAN(1),
    TEMPORARY_BAN(2),
    PERMANENT_MUTE(3),
    TEMPORARY_MUTE(4);
    private @Getter final int id;

    public boolean isBanned() {
        return this.id == 1 || this.id == 2;
    }

    public boolean isMuted() {
        return !isBanned();
    }

    public boolean isPermanent() {
        return this.id == 1 || this.id == 3;
    }

    public @Nullable static PunishmentType fromId(int id) {
        for (PunishmentType punishmentType : values()) {
            if (punishmentType.id == id) return punishmentType;
        }
        return null;
    }
}
