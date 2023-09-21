package com.uroria.backend.impl.permission.impl;

import com.uroria.backend.permission.Permission;
import com.uroria.base.permission.PermState;
import lombok.NonNull;

import static com.uroria.base.permission.PermState.*;

public final class PermissionImpl implements Permission {
    private final String node;
    private int state;

    public PermissionImpl(@NonNull String node, @NonNull PermState state) {
        this.node = node;
        setState(state);
    }

    @Override
    public void setState(@NonNull PermState state) {
        switch (state) {
            case NOT_SET -> this.state = 0;
            case TRUE -> this.state = 1;
            case FALSE -> this.state = 2;
        }
    }

    @Override
    public String getNode() {
        return this.node;
    }

    @Override
    public PermState getState() {
        switch (state) {
            case 1 -> {
                return TRUE;
            }
            case 2 -> {
                return FALSE;
            }
            default -> {
                return NOT_SET;
            }
        }
    }
}
