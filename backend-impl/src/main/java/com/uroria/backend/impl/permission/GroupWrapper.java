package com.uroria.backend.impl.permission;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.uroria.backend.Deletable;
import com.uroria.backend.impl.pulsar.PulsarObject;
import com.uroria.backend.impl.pulsar.Result;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.Permission;
import com.uroria.base.permission.PermState;
import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class GroupWrapper implements PermGroup {
    private final PulsarObject object;
    private final String name;
    private final String prefix;
    private final ObjectSet<Permission> permissions;
    private boolean deleted;

    public GroupWrapper(@NonNull PulsarObject object, @NonNull String name) {
        this.object = object;
        this.name = name.toLowerCase();
        this.permissions = new ObjectArraySet<>();
        this.prefix = "group." + name + ".";
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getPriority() {
        Deletable.checkDeleted(this);
        Result<JsonElement> result = object.get(prefix + "priority");
        JsonElement element = result.get();
        if (element == null) return 999;
        return element.getAsInt();
    }

    @Override
    public void delete() {
        if (isDeleted()) return;
        this.object.set(prefix + "deleted", true);
        this.object.remove("group." + name);
    }

    @Override
    public boolean isDeleted() {
        if (this.deleted) return true;
        Result<JsonElement> result = object.get(prefix + "deleted");
        JsonElement element = result.get();
        if (element == null) return false;
        boolean deleted = element.getAsBoolean();
        if (deleted) this.deleted = true;
        return deleted;
    }

    private void setPermission(String node, boolean value) {
        this.permissions.removeIf(perm -> perm.getNode().equals(node));
        PermState state;
        if (value) state = PermState.TRUE;
        else state = PermState.FALSE;
        this.permissions.add(getImpl(node, state));
        if (value) {
            List<String> allowed = getStringArray(prefix + "allowed");
            allowed.removeIf(someNode -> someNode.equals(node));
            allowed.add(node);
            setStringArray(allowed, prefix + "allowed");
            return;
        }
        List<String> disallowed = getStringArray(prefix + "disallowed");
        disallowed.removeIf(someNode -> someNode.equals(node));
        disallowed.add(node);
        setStringArray(disallowed, prefix + "disallowed");
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
        List<String> allowed = getStringArray(prefix + "allowed");
        List<String> disallowed = getStringArray(prefix + "disallowed");
        allowed.remove(node);
        disallowed.remove(node);
        setStringArray(allowed, prefix + "allowed");
        setStringArray(disallowed, prefix + "disallowed");
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
        List<String> allowed = getRawAllowed();
        List<String> disallowed = getRawDisallowed();
        allowed.forEach(string -> map.put(string, true));
        disallowed.forEach(string -> map.put(string, false));
        return map;
    }

    private List<String> getRawAllowed() {
        return getStringArray(prefix + ".allowed");
    }

    private List<String> getRawDisallowed() {
        return getStringArray(prefix + ".disallowed");
    }

    private List<String> getStringArray(String key) {
        Result<JsonElement> result = this.object.get(key);
        JsonElement element = result.get();
        if (element == null) return ObjectLists.emptyList();
        JsonArray stringArray = element.getAsJsonArray();
        return stringArray.asList().stream()
                .map(JsonElement::getAsString)
                .toList();
    }

    private void setStringArray(List<String> list, String key) {
        Result<JsonElement> result = this.object.get(key);
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
}
