package com.uroria.backend.bukkit.listeners;

import com.uroria.backend.bukkit.BackendAPI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

public record PlayerPreLogin(BackendAPI backendAPI) implements Listener {

    @EventHandler
    public void onPlayerPreLoginEvent(AsyncPlayerPreLoginEvent preLoginEvent) {
        UUID uuid = preLoginEvent.getUniqueId();
        this.backendAPI.getPlayerManager().getPlayer(uuid, 10000);
        this.backendAPI.getPermissionManager().getPermissionHolder(uuid, 10000).ifPresent(holder -> {
            holder.getGroups().forEach(group -> {
                this.backendAPI.getPermissionManager().getPermissionGroup(group, 2000);
            });
        });
    }
}
