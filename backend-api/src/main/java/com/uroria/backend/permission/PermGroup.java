package com.uroria.backend.permission;

import com.uroria.backend.Backend;
import com.uroria.backend.property.PropertyObject;
import com.uroria.backend.utils.ObjectUtils;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public final class PermGroup extends PropertyObject<PermGroup> implements Serializable {
    @Serial private static final long serialVersionUID = 1;

    private @Getter final String name;
    private final Object2BooleanMap<String> permissions;
    private @Getter @Setter int priority;

    public PermGroup(@NonNull String name, int priority) {
        this.name = name.toLowerCase();
        this.priority = priority;
        this.permissions = new Object2BooleanArrayMap<>();
    }

    public PermGroup(@NonNull String name) {
        this(name, 999);
    }

    public boolean hasPermission(@Nullable String node) {
        if (node == null) return false;
        return PermCalculator.hasPermission(node, this.permissions);
    }

    public void setPermission(@NonNull String node, boolean value) {
        this.permissions.put(node, value);
    }

    public void unsetPermission(String node) {
        if (node == null) return;
        this.permissions.remove(node);
    }

    public Map<String, Boolean> getPermissions() {
        return Collections.unmodifiableMap(this.permissions);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PermGroup group)) return false;
        return group.getName().equals(this.name);
    }

    @Override
    public String toString() {
        return "PermGroup{name="+this.name+", priority="+this.priority+"}";
    }

    @Override
    public void modify(PermGroup group) {
        this.deleted = group.deleted;
        this.priority = group.priority;
        ObjectUtils.overrideMap(this.permissions, group.permissions);
    }

    @Override
    public void update() {
        Backend.getAPI().getPermissionManager().updateGroup(this);
    }
}
