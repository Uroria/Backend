package com.uroria.backend.common.helpers;

public enum PunishmentType {
    PERMANENT_BAN(1),
    TEMPORARY_BAN(2),
    PERMANENT_MUTE(3),
    TEMPORARY_MUTE(4);
    private final int id;
    PunishmentType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public boolean isBanned() {
        return this.id == 1 || this.id == 2;
    }

    public boolean isMuted() {
        return !isBanned();
    }

    public boolean isPermanent() {
        return this.id == 1 || this.id == 3;
    }

    public static PunishmentType fromId(int id) {
        for (PunishmentType punishmentType : values()) {
            if (punishmentType.id == id) return punishmentType;
        }
        return null;
    }
}
