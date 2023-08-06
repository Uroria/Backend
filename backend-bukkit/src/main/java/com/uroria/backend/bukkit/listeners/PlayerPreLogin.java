package com.uroria.backend.bukkit.listeners;

import com.uroria.backend.bukkit.BackendImpl;
import com.uroria.backend.permission.PermGroup;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.List;
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
        backend.getPermissionManager().getHolder(uuid, 10000).ifPresentOrElse(holder -> {
            List<PermGroup> groups = holder.getGroups();
            if (!groups.isEmpty()) return;
            holder.addGroup(getDefaultGroup());
            holder.update();
        }, () -> {
            preLoginEvent.setKickMessage("Backend Permission timeout");
            preLoginEvent.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        });
    }

    private PermGroup getDefaultGroup() {
        PermGroup group = backend.getPermissionManager().getGroup("default", 2000).orElse(null);
        if (group == null) {
            group = new PermGroup("default");
            group.update();
        }
        return group;
    }
}
