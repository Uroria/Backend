package com.uroria.backend.bukkit.listeners;

import com.uroria.backend.bukkit.BackendImpl;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

public record PlayerPreLogin(BackendImpl backend) implements Listener {

    @EventHandler (priority = EventPriority.LOWEST)
    public void onPlayerPreLoginEvent(AsyncPlayerPreLoginEvent preLoginEvent) {
        UUID uuid = preLoginEvent.getUniqueId();
        String name = preLoginEvent.getName();
        backend.getUserManager().getUser(uuid, 10000).ifPresentOrElse(user -> {
            if (user.getUsername() == null) {
                user.setUsername(name);
                user.update();
            }
        }, () -> {
            preLoginEvent.setKickMessage("Backend User timeout");
            preLoginEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        });

        if (backend.getPermissionManager().getHolder(uuid, 10000).isEmpty()) {
            preLoginEvent.setKickMessage("Backend Permission timeout");
            preLoginEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }
    }
}
