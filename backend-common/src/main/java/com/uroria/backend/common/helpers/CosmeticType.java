package com.uroria.backend.common.helpers;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

public enum CosmeticType {
    HAT(0),
    BALLOON(1),
    BACKPACK(2),
    EMOTE(3);

    private @Getter final int id;

    CosmeticType(int id) {
        this.id = id;
    }

    public static @Nullable CosmeticType getByID(int id) {
        for (CosmeticType type : values()) {
            if (type.id == id) return type;
        }
        return null;
    }
}
