package com.uroria.backend.bukkit.listeners;

import com.uroria.backend.bukkit.BackendAPIImpl;
import com.uroria.backend.server.BackendServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.concurrent.CompletableFuture;

public record PlayerJoin() implements Listener {
    
    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerJoinEvent(PlayerJoinEvent playerJoinEvent) {
        CompletableFuture.runAsync(() -> {
            try {
                BackendServer server = BackendAPIImpl.getAPI().getServerManager().getThisServer();
                server.addPlayer(playerJoinEvent.getPlayer().getUniqueId());
                BackendAPIImpl.getAPI().getServerManager().updateServer(server);
            } catch (Exception ignored) {}
        });
    }
}
