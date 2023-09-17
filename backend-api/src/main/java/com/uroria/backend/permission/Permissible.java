package com.uroria.backend.permission;

public interface Permissible {
    Permission getPermission(String node);

    default boolean hasPermission(String node) {
        return getPermission(node).isGiven();
    }
}
