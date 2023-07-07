package com.uroria.backend.common;

import com.uroria.backend.common.utils.ObjectUtils;
import com.uroria.backend.common.utils.PermissionCalculator;
import com.uroria.backend.common.utils.TransientField;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public final class PermissionHolder extends BackendObject<PermissionHolder> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID uuid;
    private final Map<String, Boolean> permissions;
    @TransientField private Map<String, Boolean> temporaryPermissions;
    private final List<String> groups;
    public PermissionHolder(@NonNull UUID uuid) {
        this.uuid = uuid;
        this.permissions = new HashMap<>();
        this.temporaryPermissions = new HashMap<>();
        this.groups = new ArrayList<>();
    }

    public boolean hasPermission(@Nullable String node) {
        if (node == null) return false;
        if (temporaryPermissions == null) temporaryPermissions = new HashMap<>();
        boolean base = PermissionCalculator.hasPermission(node, permissions);
        boolean temp = PermissionCalculator.hasPermission(node, temporaryPermissions);
        if (base) return true;
        return temp;
    }

    public void addGroup(@NonNull String group) {
        this.groups.add(group);
    }

    public void removeGroup(@NonNull String group) {
        this.groups.remove(group);
    }

    public void setPermission(@NonNull String node, boolean value) {
        this.permissions.put(node, value);
    }

    public void unsetPermission(@NonNull String node) {
        this.permissions.remove(node);
    }

    public void setTemporaryPermission(@NonNull String node, boolean value) {
        if (this.temporaryPermissions == null) this.temporaryPermissions = new HashMap<>();
        this.temporaryPermissions.put(node, value);
    }

    public void unsetTemporaryPermission(@NonNull String node) {
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
            return holder.getUUID().equals(getUUID());
        }
        return false;
    }

    @Override
    public void modify(PermissionHolder holder) {
        ObjectUtils.overrideMap(permissions, holder.permissions);
        ObjectUtils.overrideCollection(groups, holder.groups);
        if (this.temporaryPermissions == null) this.temporaryPermissions = new HashMap<>();
        ObjectUtils.overrideMap(temporaryPermissions, holder.temporaryPermissions);
    }
}
