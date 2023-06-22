package com.uroria.backend.common;

import com.uroria.backend.common.utils.PermissionCalculator;
import com.uroria.backend.common.utils.TransientField;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PermissionHolder extends BackendObject<PermissionHolder> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID uuid;
    private final Map<String, Boolean> permissions;
    @TransientField
    private Map<String, Boolean> temporaryPermissions;
    private final Collection<String> groups;
    public PermissionHolder(UUID uuid) {
        this.uuid = uuid;
        this.permissions = new HashMap<>();
        this.temporaryPermissions = new HashMap<>();
        this.groups = new ArrayList<>();
    }

    public boolean hasPermission(String node) {
        if (temporaryPermissions == null) temporaryPermissions = new HashMap<>();
        boolean base = PermissionCalculator.hasPermission(node, permissions);
        boolean temp = PermissionCalculator.hasPermission(node, temporaryPermissions);
        if (base) return true;
        return temp;
    }

    public void addGroup(String group) {
        this.groups.add(group);
    }

    public void removeGroup(String group) {
        this.groups.remove(group);
    }

    public void setPermission(String node, boolean value) {
        this.permissions.put(node, value);
    }

    public void unsetPermission(String node) {
        this.permissions.remove(node);
    }

    public void setTemporaryPermission(String node, boolean value) {
        if (this.temporaryPermissions == null) this.temporaryPermissions = new HashMap<>();
        this.temporaryPermissions.put(node, value);
    }

    public void unsetTemporaryPermission(String node) {
        if (this.temporaryPermissions == null) this.temporaryPermissions = new HashMap<>();
        this.temporaryPermissions.remove(node);
    }

    public void flushTemporaryPermissions() {
        if (this.temporaryPermissions == null) this.temporaryPermissions = new HashMap<>();
        this.temporaryPermissions.clear();
    }

    public UUID getUUID() {
        return uuid;
    }

    public Map<String, Boolean> getPermissions() {
        return new HashMap<>(this.permissions);
    }

    public Map<String, Boolean> getTemporaryPermissions() {
        return new HashMap<>(temporaryPermissions);
    }

    public Collection<String> getGroups() {
        return new ArrayList<>(this.groups);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof PermissionHolder holder) {
            if (holder.getUUID().equals(getUUID())) return true;
        }
        return false;
    }

    @Override
    public void modify(PermissionHolder holder) {
        permissions.clear();
        permissions.putAll(holder.permissions);

        groups.clear();
        groups.addAll(holder.groups);
    }
}
