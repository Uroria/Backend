package com.uroria.backend.common.helpers;

public enum PunishmentType {
    NONE(0),
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

    public static PunishmentType fromId(int id) {
        for (PunishmentType punishmentType : values()) {
            if (punishmentType.id == id) return punishmentType;
        }
        return null;
    }
}
