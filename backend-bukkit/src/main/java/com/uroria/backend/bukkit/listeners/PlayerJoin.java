package com.uroria.backend.bukkit.listeners;

import com.uroria.backend.bukkit.BackendBukkitPlugin;
import com.uroria.backend.server.Server;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public record PlayerJoin(BackendBukkitPlugin plugin, Logger logger) implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent joinEvent) {
        CompletableFuture.runAsync(() -> {
            try {
                Server server = plugin.getServerManager().getServer();
                if (server == null) return;
                server.addPlayer(joinEvent.getPlayer().getUniqueId());
                server.update();
            } catch (Exception exception) {
                this.logger.error("Cannot add player to server", exception);
            }
        });
    }
}
