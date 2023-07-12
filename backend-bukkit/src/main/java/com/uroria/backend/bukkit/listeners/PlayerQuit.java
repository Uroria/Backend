package com.uroria.backend.bukkit.listeners;

import com.uroria.backend.bukkit.BackendAPIImpl;
import com.uroria.backend.server.BackendServer;
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
                BackendServer server = BackendAPIImpl.getAPI().getServerManager().getThisServer();
                server.removePlayer(playerQuitEvent.getPlayer().getUniqueId());
                BackendAPIImpl.getAPI().getServerManager().updateServer(server);
            } catch (Exception ignored) {}
        });
    }
}
