package com.uroria.backend.bukkit.listeners;

import com.uroria.backend.bukkit.BackendAPI;
import com.uroria.backend.common.BackendServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.CompletableFuture;

public record PlayerQuit() implements Listener {
    
    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent(PlayerQuitEvent playerQuitEvent) {
        CompletableFuture.runAsync(() -> {
            try {
                BackendServer server = BackendAPI.getAPI().getServerManager().getThisServer();
                server.removePlayer(playerQuitEvent.getPlayer().getUniqueId());
                BackendAPI.getAPI().getServerManager().updateServer(server);
            } catch (Exception ignored) {}
        });
    }
}
