package com.uroria.backend.permission;

import com.uroria.backend.helpers.PropertyHolder;
import com.uroria.backend.utils.ObjectUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public final class PermissionGroup extends PropertyHolder<PermissionGroup> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final String name;
    private final Map<String, Boolean> permissions;
    private int priority;
    public PermissionGroup(@NonNull String name, int priority) {
        this.name = name.toLowerCase();
        this.permissions = new Object2ObjectArrayMap<>();
        this.priority = priority;
    }

    public boolean hasPermission(@Nullable String node) {
        if (node == null) return false;
        return PermissionCalculator.hasPermission(node, permissions);
    }

    public void setPermission(@NonNull String node, boolean value) {
        this.permissions.put(node, value);
    }

    public void unsetPermission(@NonNull String node) {
        this.permissions.remove(node);
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public Map<String, Boolean> getPermissions() {
        return Collections.unmodifiableMap(this.permissions);
    }

    @Override
    public void modify(PermissionGroup group) {
        priority = group.getPriority();
        ObjectUtils.overrideMap(permissions, group.permissions);
        ObjectUtils.overrideMap(properties, group.properties);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof PermissionGroup group) {
            return group.getName().equals(getName());
        }

        return false;
    }
}
