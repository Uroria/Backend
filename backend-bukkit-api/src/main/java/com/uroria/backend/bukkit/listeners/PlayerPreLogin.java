package com.uroria.backend.bukkit.listeners;

import com.uroria.backend.bukkit.BackendAPIImpl;
import com.uroria.backend.common.permission.PermissionHolder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

public record PlayerPreLogin(BackendAPIImpl backendAPI) implements Listener {

    @EventHandler
    public void onPlayerPreLoginEvent(AsyncPlayerPreLoginEvent preLoginEvent) {
        UUID uuid = preLoginEvent.getUniqueId();
        String name = preLoginEvent.getName();
        this.backendAPI.getPlayerManager().getPlayer(uuid, 10000).ifPresent(player -> {
            if (player.getCurrentName().isEmpty()) {
                player.setCurrentName(name);
                this.backendAPI.getPlayerManager().updatePlayer(player);
            }
        });
        this.backendAPI.getPermissionManager().getHolder(uuid, 10000).ifPresent(PermissionHolder::getGroups);
    }
}
