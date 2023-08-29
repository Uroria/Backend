package com.uroria.backend.bukkit.listeners;

import com.uroria.backend.bukkit.BackendBukkitPlugin;
import com.uroria.backend.bukkit.BackendImpl;
import com.uroria.backend.server.Server;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public record PlayerQuit(BackendImpl backend, Logger logger, BackendBukkitPlugin plugin) implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onPlayerQuitEvent(PlayerQuitEvent quitEvent) {
        CompletableFuture.runAsync(() -> {
            int size = Bukkit.getOnlinePlayers().size();
            if (size < 3) plugin.checkTimeout();
            try {
                Server server = backend.getServerManager().getServer();
                if (server == null) return;
                server.addPlayer(quitEvent.getPlayer().getUniqueId());
                server.update();
            } catch (Exception exception) {
                this.logger.error("Cannot remove player from server", exception);
            }
        });
    }
}
