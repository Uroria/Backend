package com.uroria.backend.impl.permission;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.uroria.backend.Deletable;
import com.uroria.backend.impl.communication.CommunicationClient;
import com.uroria.backend.impl.communication.CommunicationWrapper;
import com.uroria.backend.impl.wrapper.Wrapper;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.Permission;
import com.uroria.base.permission.PermState;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public final class GroupWrapper extends Wrapper implements PermGroup {
    private final CommunicationWrapper object;
    private final String name;
    private final ObjectSet<Permission> permissions;
    private boolean deleted;

    public GroupWrapper(@NonNull CommunicationClient client, @NonNull String name) {
        this.object = new CommunicationWrapper(name, client);
        this.name = name.toLowerCase();
        this.permissions = new ObjectArraySet<>();
    }

    @Override
    public void refresh() {
        refreshPermissions();
    }

    public JsonObject getObject() {
        return this.object.getObject();
    }

    @Override
    public CommunicationWrapper getObjectWrapper() {
        return this.object;
    }

    @Override
    public String getIdentifierKey() {
        return "name";
    }

    @Override
    public String getStringIdentifier() {
        return this.name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getPriority() {
        Deletable.checkDeleted(this);
        return getInt("priority", 999);
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.object.set("deleted", true);
        this.object.remove("group." + name);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        boolean deleted = getBoolean("deleted");
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
            Collection<String> allowed = getStrings("allowed");
            allowed.removeIf(someNode -> someNode.equals(node));
            allowed.add(node);
            setStringArray(allowed, "allowed");
            return;
        }
        Collection<String> disallowed = getStrings("disallowed");
        disallowed.removeIf(someNode -> someNode.equals(node));
        disallowed.add(node);
        setStringArray(disallowed, "disallowed");
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
        Collection<String> allowed = getStrings("allowed");
        Collection<String> disallowed = getStrings("disallowed");
        allowed.remove(node);
        disallowed.remove(node);
        setStringArray(allowed, "allowed");
        setStringArray(disallowed, "disallowed");
    }

    @Override
    public @NotNull Permission getPermission(String node) {
        Deletable.checkDeleted(this);
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

    private Collection<String> getRawAllowed() {
        return getStrings("allowed");
    }

    private Collection<String> getRawDisallowed() {
        return getStrings("disallowed");
    }

    private void setStringArray(Collection<String> list, String key) {
        JsonArray array = new JsonArray();
        list.forEach(array::add);
        this.object.set(key, array);
    }

    @Override
    public synchronized void refreshPermissions() {
        Deletable.checkDeleted(this);
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
        Deletable.checkDeleted(this);
        return this.permissions;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Object2ObjectMaps.emptyMap();
    }

    @Override
    public void unsetProperty(@NonNull String key) {
        this.object.set("property." + key, JsonNull.INSTANCE);
    }

    @Override
    public void setProperties(@NonNull Map<String, Object> properties) {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Integer n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Long n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Double n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Float n) {
                setProperty("property." + key, n);
                return;
            }
            if (value instanceof Boolean b) {
                setProperty("property." + key, b);
                return;
            }
            if (value instanceof String s) {
                setProperty("property." + key, s);
                return;
            }
        }
    }

    @Override
    public void setProperty(@NonNull String key, @NonNull String value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, int value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, long value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, double value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, float value) {
        this.object.set("property." + key, value);
    }

    @Override
    public void setProperty(@NonNull String key, boolean value) {
        this.object.set("property." + key, value);
    }

    @Override
    public String getPropertyStringOrElse(@NonNull String key, @Nullable String defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsString();
    }

    @Override
    public int getPropertyIntOrElse(@NonNull String key, int defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsInt();
    }

    @Override
    public long getPropertyLongOrElse(@NonNull String key, long defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsLong();
    }

    @Override
    public double getPropertyDoubleOrElse(@NonNull String key, double defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsDouble();
    }

    @Override
    public float getPropertyFloatOrElse(@NonNull String key, float defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsFloat();
    }

    @Override
    public boolean getPropertyBooleanOrElse(@NonNull String key, boolean defValue) {
        JsonElement element = this.object.get("property." + key).get();
        if (element == null) return defValue;
        return element.getAsBoolean();
    }
}
