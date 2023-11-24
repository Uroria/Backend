package com.uroria.backend.velocity.perm;

import com.uroria.backend.Backend;
import com.uroria.backend.permission.Permission;
import com.uroria.backend.user.User;
import com.velocitypowered.api.permission.PermissionFunction;
import com.velocitypowered.api.permission.PermissionProvider;
import com.velocitypowered.api.permission.PermissionSubject;
import com.velocitypowered.api.permission.Tristate;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

@AllArgsConstructor
public final class BackendPermissionProvider implements PermissionProvider {
    private static final PermissionFunction acceptFunction = permission -> Tristate.TRUE;
    private static final PermissionFunction undefinedFunction = permission -> Tristate.UNDEFINED;

    private final Logger logger;

    @Override
    public PermissionFunction createFunction(PermissionSubject subject) {
        if (subject instanceof Player player) {
            User user = Backend.user(player.getUniqueId()).get();
            if (user == null) {
                player.disconnect(Component.text("Unable to apply permissions due to missing backend information", NamedTextColor.RED));
                return undefinedFunction;
            }
            return node -> {
                Permission permission = user.getPermission(node);
                return switch (permission.getState()) {
                    case TRUE -> Tristate.TRUE;
                    case FALSE -> Tristate.FALSE;
                    case NOT_SET -> Tristate.UNDEFINED;
                };
            };
        }

        if (subject instanceof ConsoleCommandSource) return acceptFunction;

        return undefinedFunction;
    }
}
