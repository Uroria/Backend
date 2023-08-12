package com.uroria.backend.bukkit.permission.listeners;

import com.uroria.backend.bukkit.permission.PermissionObject;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.lang.reflect.Field;

public final class PlayerLogin implements Listener {
    private final Field permField = getPermField();

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent loginEvent) {
        if (permField == null) {
            loginEvent.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Permission field null");
            Bukkit.getLogger().severe("Permission field is null");
            return;
        }
        Player player = loginEvent.getPlayer();

        try {
            permField.setAccessible(true);
            permField.set(player, new PermissionObject(player));
            permField.setAccessible(false);
        } catch (IllegalAccessException exception) {
            loginEvent.disallow(PlayerLoginEvent.Result.KICK_OTHER, "§cUnresolved permission object!");
        }
    }

    private static Field getPermField() {
        try {
            return Class.forName("org.bukkit.craftbukkit.v1_20_R1.entity.CraftHumanEntity").getDeclaredField("perm");
        } catch (Exception exception) {
            return null;
        }
    }
}
