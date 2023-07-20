package com.uroria.backend.permission;

import com.uroria.backend.BackendAPI;
import com.uroria.backend.BackendObject;
import com.uroria.backend.utils.ObjectUtils;
import com.uroria.backend.utils.TransientField;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class PermissionHolder extends BackendObject<PermissionHolder> implements Serializable {
    @Serial private static final long serialVersionUID = 1;
    private final UUID uuid;
    private final Map<String, Boolean> permissions;
    @TransientField private Map<String, Boolean> temporaryPermissions;
    private final Set<String> groups;
    public PermissionHolder(@NonNull UUID uuid) {
        this.uuid = uuid;
        this.permissions = new ConcurrentHashMap<>();
        this.temporaryPermissions = new ConcurrentHashMap<>();
        this.groups = ObjectUtils.newSet();
    }

    public boolean hasPermission(@Nullable String node) {
        if (node == null) return false;
        if (temporaryPermissions == null) temporaryPermissions = new ConcurrentHashMap<>();
        boolean base = PermissionCalculator.hasPermission(node, permissions);
        boolean temp = PermissionCalculator.hasPermission(node, temporaryPermissions);
        if (base) return true;
        return temp;
    }

    public void addGroup(@NonNull PermissionGroup group) {
        this.groups.add(group.getName());
    }

    public void removeGroup(@NonNull PermissionGroup group) {
        this.groups.remove(group.getName());
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

    public Set<PermissionGroup> getGroups() {
        return groups.stream().map(name -> BackendAPI.getAPI().getPermissionManager().getGroup(name).orElse(null)).filter(Objects::nonNull).collect(Collectors.toSet());
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
        if (this.temporaryPermissions == null) this.temporaryPermissions = new ConcurrentHashMap<>();
        ObjectUtils.overrideMap(temporaryPermissions, holder.temporaryPermissions);
    }
}
