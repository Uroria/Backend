package com.uroria.backend.permission;

import com.uroria.backend.Backend;
import com.uroria.backend.BackendObject;
import com.uroria.backend.utils.ObjectUtils;
import com.uroria.backend.utils.TransientField;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class PermHolder extends BackendObject<PermHolder> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private final UUID uuid;
    private final Object2BooleanMap<String> permission;
    @TransientField private Object2BooleanMap<String> tempPermissions;
    private final List<String> groups;

    public PermHolder(@NonNull UUID uuid) {
        this.uuid = uuid;
        this.permission = new Object2BooleanArrayMap<>();
        this.tempPermissions = new Object2BooleanArrayMap<>();
        this.groups = new ObjectArrayList<>();
    }

    public boolean hasPermission(@Nullable String node) {
        if (node == null) return false;
        return PermCalculator.hasPermission(node, this.permission) || PermCalculator.hasPermission(node, this.tempPermissions);
    }

    public void setPermission(@NonNull String node, boolean value) {
        this.permission.put(node, value);
    }

    public void unsetPermission(String node) {
        if (node == null) return;
        this.permission.remove(node);
    }

    public void setTempPermission(@NonNull String node, boolean value) {
        this.tempPermissions.put(node, value);
    }

    public void unsetTempPermission(String node) {
        if (node == null) return;
        this.tempPermissions.remove(node);
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void addGroup(@NonNull PermGroup group) {
        if (this.groups.stream().anyMatch(name -> group.getName().equals(name))) return;
        this.groups.add(group.getName());
    }

    public void removeGroup(PermGroup group) {
        if (group == null) return;
        if (this.groups.stream().noneMatch(name -> group.getName().equals(name))) return;
        this.groups.remove(group.getName());
    }

    public void removeGroup(String groupName) {
        if (groupName == null) return;
        this.groups.remove(groupName.toLowerCase());
    }

    public List<String> getGroupNames() {
        return Collections.unmodifiableList(this.groups);
    }

    public List<PermGroup> getGroups() {
        return this.groups.stream().map(name -> Backend.getAPI().getPermissionManager().getGroup(name).orElse(null))
                .filter(Objects::nonNull).toList();
    }

    public Map<String, Boolean> getPermissions() {
        return Collections.unmodifiableMap(this.permission);
    }

    public Map<String, Boolean> getTempPermissions() {
        return Collections.unmodifiableMap(this.tempPermissions);
    }

    public void flushTempPermissions() {
        this.tempPermissions.clear();
    }

    @Override
    public void modify(PermHolder holder) {
        this.deleted = holder.deleted;
        ObjectUtils.overrideMap(this.permission, holder.permission);
        ObjectUtils.overrideCollection(this.groups, holder.groups);
        if (this.tempPermissions == null) this.tempPermissions = new Object2BooleanArrayMap<>();
        ObjectUtils.overrideMap(this.tempPermissions, holder.tempPermissions);
    }

    @Override
    public void update() {
        Backend.getAPI().getPermissionManager().updateHolder(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PermHolder holder)) return false;
        return this.uuid.equals(holder.uuid);
    }

    @Override
    public String toString() {
        return "PermHolder{uuid="+uuid+"}";
    }
}
