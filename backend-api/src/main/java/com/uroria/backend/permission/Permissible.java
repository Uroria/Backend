package com.uroria.backend.permission;

import it.unimi.dsi.fastutil.objects.ObjectSet;

public interface Permissible {
    Permission getPermission(String node);

    default boolean hasPermission(String node) {
        return getPermission(node).isGiven();
    }

    void refreshPermissions();

    ObjectSet<Permission> getSetPermissions();
}
