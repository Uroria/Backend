package com.uroria.backend.permission;

import lombok.experimental.UtilityClass;

import java.util.Map;

@UtilityClass
public class PermCalculator {

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
