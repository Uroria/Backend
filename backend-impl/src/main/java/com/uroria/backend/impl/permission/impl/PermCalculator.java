package com.uroria.backend.impl.permission.impl;

import com.uroria.backend.permission.Permission;
import com.uroria.base.permission.PermState;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class PermCalculator {

    public Permission getPermission(String node, ObjectSet<PermissionImpl> perms) {
        final String[] nodeParts = node.split("\\.");
        final StringBuilder currentNode = new StringBuilder();
        for (int i = 0; i < nodeParts.length; i++) {
            if (i > 0) {
                currentNode.append(".");
            }

            currentNode.append(nodeParts[i]);

            final String finalCurrentNode = currentNode.toString();

            PermissionImpl perm = perms.stream()
                    .filter(permission -> permission.getNode().equals(finalCurrentNode))
                    .findAny().orElse(null);
            if (perm != null) return perm;


            final String wildcardNode = currentNode + ".*";
            perm = perms.stream()
                    .filter(permission -> permission.getNode().equals(wildcardNode))
                    .findAny().orElse(null);
            if (perm != null) return perm;
        }
        return new PermissionImpl(node, PermState.NOT_SET);
    }

    public boolean hasPermission(String node, Map<String, Boolean> permissions) {
        final String[] nodeParts = node.split("\\.");
        String currentNode = "";
        for (int i = 0; i < nodeParts.length; i++) {
            if (i > 0) {
                currentNode += ".";
            }

            currentNode += nodeParts[i];

            Boolean allowed = permissions.get(currentNode);
            if (allowed != null) return allowed;


            final String wildcardNode = currentNode + ".*";
            allowed = permissions.get(wildcardNode);
            if (allowed != null) return allowed;
        }

        Boolean allowed = permissions.get("*");
        return allowed != null && allowed;
    }
}
