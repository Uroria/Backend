package com.uroria.backend.impl.permission;

import com.google.gson.JsonElement;
import com.uroria.backend.cache.Wrapper;
import com.uroria.backend.cache.WrapperManager;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.Permission;
import com.uroria.base.permission.PermState;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public final class GroupWrapper extends Wrapper implements PermGroup {
    private final String name;
    private final ObjectSet<Permission> permissions;
    private boolean deleted;

    public GroupWrapper(WrapperManager<? extends Wrapper> wrapperManager, @NonNull String name) {
        super(wrapperManager);
        this.name = name.toLowerCase();
        this.permissions = new ObjectArraySet<>();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getPriority() {
        return this.object.getIntOrElse("priority", 999);
    }

    @Override
    public void setPriority(int priority) {
        if (isDeleted()) return;
        this.object.set("priority", priority);
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.deleted = true;
        this.object.set("deleted", true);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        boolean deleted = this.object.getBooleanOrElse("deleted", false);
        this.deleted = deleted;
        return deleted;
    }

    private void setPermission(String node, boolean value) {
        this.permissions.removeIf(perm -> perm.getNode().equals(node));
        PermState state;
        if (value) state = PermState.TRUE;
        else state = PermState.FALSE;
        this.permissions.add(getImpl(node, state));
        if (value) {
            ObjectSet<String> allowed = this.object.getSet("allowed", String.class);
            allowed.removeIf(someNode -> someNode.equals(node));
            allowed.add(node);
            this.object.set("allowed", allowed);
            return;
        }
        ObjectSet<String> disallowed = this.object.getSet("disallowed", String.class);
        disallowed.removeIf(someNode -> someNode.equals(node));
        disallowed.add(node);
        this.object.set("disallowed", disallowed);
    }

    private void setPermission(String node, PermState state) {
        node = node.toLowerCase();
        switch (state) {
            case TRUE -> setPermission(node, true);
            case FALSE -> setPermission(node, false);
            case NOT_SET -> unsetPermission(node);
        }
    }

    private void unsetPermission(String node) {
        ObjectSet<String> allowed = getRawAllowed();
        ObjectSet<String> disallowed = getRawDisallowed();
        allowed.remove(node);
        disallowed.remove(node);
        this.object.set("allowed", allowed);
        this.object.set("disallowed", disallowed);
    }

    @Override
    public @NotNull Permission getPermission(String node) {
        Permission permission = getRootPermission(node);
        if (permission != null) return permission;

        final String[] nodeParts = node.split("\\.");
        final StringBuilder currentNode = new StringBuilder();
        int i = 0;
        while (i <= nodeParts.length) {
            if (i > 0) currentNode.append(".");

            currentNode.append(nodeParts[i]);

            String current = currentNode.toString();

            Permission rootPermission = getRootPermission(current);
            if (rootPermission != null) {
                this.permissions.add(getImpl(node, rootPermission.getState()));
                return rootPermission;
            }

            String wildcardNode = current + ".*";

            Permission wildCardPermission = getRootPermission(wildcardNode);
            if (wildCardPermission != null) {
                this.permissions.add(getImpl(node, wildCardPermission.getState()));
                return wildCardPermission;
            }

            i++;
        }

        Permission rootPermission = getRootPermission("*");
        if (rootPermission != null) {
            return rootPermission;
        }

        return getImpl(node, PermState.NOT_SET);
    }

    private Permission getRootPermission(String node) {
        final String finalNode = node.toLowerCase();
        return this.permissions.stream()
                .filter(perm -> perm.getNode().equals(finalNode))
                .findAny()
                .orElse(null);
    }

    private Object2BooleanMap<String> getRawPermissions() {
        Object2BooleanMap<String> map = new Object2BooleanArrayMap<>();
        Collection<String> allowed = getRawAllowed();
        Collection<String> disallowed = getRawDisallowed();
        allowed.forEach(string -> map.put(string, true));
        disallowed.forEach(string -> map.put(string, false));
        return map;
    }

    private ObjectSet<String> getRawAllowed() {
        return object.getSet("allowed", String.class);
    }

    private ObjectSet<String> getRawDisallowed() {
        return object.getSet("disallowed", String.class);
    }

    @Override
    public synchronized void refreshPermissions() {
        Object2BooleanMap<String> raw = getRawPermissions();
        this.permissions.removeIf(perm -> !raw.containsKey(perm.getNode()));
        for (Permission perm : this.permissions) {
            String node = perm.getNode();
            boolean allowed = raw.getBoolean(node);
            if (perm.isGiven() == allowed) continue;
            this.permissions.remove(perm);
            PermState state;
            if (allowed) state = PermState.TRUE;
            else state = PermState.FALSE;
            this.permissions.add(getImpl(node, state));
        }
    }

    private Permission getImpl(final String node, final PermState finalState) {
        return new Permission() {
            private PermState state = finalState;

            @Override
            public void setState(@NonNull PermState state) {
                this.state = state;
                setPermission(node, state);
            }

            @Override
            public String getNode() {
                return node;
            }

            @Override
            public PermState getState() {
                return this.state;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof Permission perm) {
                    return perm.getNode().equals(this.getNode());
                }
                return false;
            }
        };
    }

    @Override
    public ObjectSet<Permission> getSetPermissions() {
        return this.permissions;
    }

    @Override
    public Map<String, Object> getProperties() {
        return this.object.getMap("properties", Object.class);
    }

    @Override
    public void unsetProperty(@NonNull String key) {
        Map<String, Object> properties = getProperties();
        properties.remove(key);
        this.object.set("properties", properties);
    }

    public void setProperty(String key, Object value) {
        Map<String, Object> properties = getProperties();
        properties.put(key, value);
        this.object.set("properties", properties);
    }

    @Override
    public void setProperties(@NonNull Map<String, Object> properties) {
        this.object.set("properties", new Object2ObjectArrayMap<>(properties));
    }

    @Override
    public void setProperty(@NonNull String key, @NonNull String value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, int value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, long value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, double value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, float value) {
        setProperty(key, (Object) value);
    }

    @Override
    public void setProperty(@NonNull String key, boolean value) {
        setProperty(key, (Object) value);
    }

    @Override
    public String getPropertyStringOrElse(@NonNull String key, @Nullable String defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (String) obj;
    }

    @Override
    public int getPropertyIntOrElse(@NonNull String key, int defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (int) obj;
    }

    @Override
    public long getPropertyLongOrElse(@NonNull String key, long defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (long) obj;
    }

    @Override
    public double getPropertyDoubleOrElse(@NonNull String key, double defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (double) obj;
    }

    @Override
    public float getPropertyFloatOrElse(@NonNull String key, float defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (float) obj;
    }

    @Override
    public boolean getPropertyBooleanOrElse(@NonNull String key, boolean defValue) {
        Object obj = getProperties().get(key);
        if (obj == null) return defValue;
        return (boolean) obj;
    }

    @Override
    public String getIdentifier() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupWrapper wrapper) {
            return wrapper.name.equals(this.name);
        }
        return false;
    }
}
