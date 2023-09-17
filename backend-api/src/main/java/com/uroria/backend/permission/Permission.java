package com.uroria.backend.permission;

import com.uroria.base.permission.PermState;
import lombok.NonNull;

public interface Permission {
    void setState(@NonNull PermState state);

    String getNode();

    default boolean isGiven() {
        return getState().asBooleanValue();
    }

    PermState getState();
}
