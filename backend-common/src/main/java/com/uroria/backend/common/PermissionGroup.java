package com.uroria.backend.common;

import com.uroria.backend.common.helpers.PropertyHolder;
import com.uroria.backend.common.utils.PermissionCalculator;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public final class PermissionGroup extends PropertyHolder implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final String name;
    private final Map<String, Boolean> permissions;
    private int priority;
    public PermissionGroup(String name, int priority) {
        this.name = name;
        this.permissions = new HashMap<>();
        this.priority = priority;
    }

    public boolean hasPermission(String node) {
        return PermissionCalculator.hasPermission(node, permissions);
    }

    public void setPermission(String node, boolean value) {
        this.permissions.put(node, value);
    }

    public void unsetPermission(String node) {
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
        return new HashMap<>(this.permissions);
    }
}
