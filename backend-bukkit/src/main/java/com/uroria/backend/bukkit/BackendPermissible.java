package com.uroria.backend.bukkit;

import com.uroria.backend.user.User;
import com.uroria.base.permission.PermState;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class BackendPermissible implements Permissible {
    private final User user;

    public BackendPermissible(User user) {
        this.user = user;
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return !user.getPermission(name).getState().equals(PermState.NOT_SET);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return isPermissionSet(perm.getName());
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return user.hasPermission(name);
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return hasPermission(perm.getName());
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        PermissionAttachment attachment = addAttachment(plugin);
        attachment.setPermission(name, value);
        return attachment;
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return new PermissionAttachment(plugin, this);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return null;
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return null;
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {

    }

    @Override
    public void recalculatePermissions() {
        this.user.refreshPermissions();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return ObjectSets.emptySet();
    }

    @Override
    public boolean isOp() {
        return false;
    }

    @Override
    public void setOp(boolean value) {

    }
}
