package com.uroria.backend.permission;

import com.uroria.annotations.markers.WeakWarning;
import com.uroria.annotations.safety.TimeConsuming;
import com.uroria.base.command.Commander;
import it.unimi.dsi.fastutil.objects.ObjectSet;

public interface Permissible {

    @WeakWarning(message = "Call could take some time if permissions aren't cached.", suppress = "I understand the problem")
    Permission getPermission(String node);

    @WeakWarning(message = "Call could take some time if permissions aren't cached.", suppress = "I understand the problem")
    default boolean hasPermission(String node) {
        //noinspection WeakWarningMarkers
        return getPermission(node).isGiven();
    }

    void refreshPermissions();

    @TimeConsuming
    ObjectSet<Permission> getSetPermissions();
}
