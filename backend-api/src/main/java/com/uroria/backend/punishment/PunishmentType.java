package com.uroria.backend.punishment;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum PunishmentType {
    NONE(0),
    PERMANENT_BAN(1),
    TEMPORARY_BAN(2),
    PERMANENT_MUTE(3),
    TEMPORARY_MUTE(4);

    private final int id;

    public int getID() {
        return this.id;
    }

    public boolean isBan() {
        return switch (this) {
            case PERMANENT_BAN, TEMPORARY_BAN -> true;
            default -> false;
        };
    }

    public boolean isMute() {
        return switch (this) {
            case PERMANENT_MUTE, TEMPORARY_MUTE -> true;
            default -> false;
        };
    }

    public boolean isPermanent() {
        return switch (this) {
            case PERMANENT_BAN, PERMANENT_MUTE -> true;
            default -> false;
        };
    }

    public boolean isValid() {
        return this != NONE;
    }

    public static PunishmentType byID(int id) {
        return Arrays.stream(values()).filter(t -> t.id == id).findAny().orElse(null);
    }
}
