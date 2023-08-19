package com.uroria.backend.bukkit.permission;
import com.uroria.backend.Backend;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.PermHolder;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PermissionObject extends PermissibleBase {
    private final Player player;
    public PermissionObject(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public boolean hasPermission(String node) {
        if (node == null) return false;
        PermHolder permissionHolder = Backend.getAPI().getPermissionManager().getHolder(this.player.getUniqueId()).orElse(null);

        if (permissionHolder == null) return false;

        List<PermGroup> groups = permissionHolder.getGroups();

        boolean group = groups.stream().min(Comparator.comparing(PermGroup::getPriority))
                .map(group1 -> group1.hasPermission(node)).orElse(false);
        boolean holder = permissionHolder.hasPermission(node);

        return group || holder;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        if (perm == null) return false;
        return hasPermission(perm.getName());
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {

    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return true;
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return true;
    }

    @Override
    public synchronized void removeAttachment(@NotNull PermissionAttachment attachment) {

    }

    @Override
    public synchronized void recalculatePermissions() {

    }

    @Override
    public synchronized void clearPermissions() {

    }

    @Override
    public synchronized PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return null;
    }

    @Override
    public synchronized PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return null;
    }

    @Override
    public synchronized @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return new HashSet<>();
    }
}
