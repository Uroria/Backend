package com.uroria.backend.impl.permission;

import com.uroria.backend.impl.permission.impl.PermCalculator;
import com.uroria.backend.impl.permission.impl.PermissionImpl;
import com.uroria.backend.permission.Permission;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;

public final class PermGroupWrapper extends AbstractPermGroup {
    private final Object2ObjectMap<String, Permission> cachedPerms;

    public PermGroupWrapper(AbstractPermManager permManager, String name) {
        super(permManager, name);
        this.cachedPerms = new Object2ObjectArrayMap<>();
    }

    @Override
    public String getName() {

    }

    @Override
    public int getPriority() {

    }

    @Override
    public Permission getPermission(@NonNull String node) {
        Permission cachedPermission = this.cachedPerms.get(node);
        if (cachedPermission == null) {
            Permission permission = PermCalculator.getPermission(node, getPerms());
            this.cachedPerms.put(node, permission);
            return permission;
        }
        return cachedPermission;
    }

    private ObjectSet<PermissionImpl> getPerms() {
        return null;
    }
}
