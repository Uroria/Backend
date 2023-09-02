package com.uroria.backend.velocity.listeners;

import com.uroria.backend.permission.PermHolder;
import com.uroria.backend.punishment.Punished;
import com.uroria.backend.user.User;
import com.uroria.backend.velocity.BackendVelocityPlugin;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.UUID;

public record PlayerLogin(BackendVelocityPlugin plugin) {
    
    @Subscribe (order = PostOrder.FIRST)
    public EventTask onPlayerLoginEvent(LoginEvent loginEvent) {
        return EventTask.async(() -> {
            Player player = loginEvent.getPlayer();
            UUID uuid = player.getUniqueId();
            PermHolder holder = plugin.getBackend().getPermissionManager().getHolder(uuid, 10000).orElse(null);
            if (holder == null) {
                holder = new PermHolder(uuid);
                holder.update();
            }
            boolean updateUser = false;
            User user = plugin.getBackend().getUserManager().getUser(uuid, 10000).orElse(null);
            if (user == null) {
                user = new User(uuid);
                user.setUsername(player.getUsername());
                updateUser = true;
            } else {
                if (!user.getUsername().equalsIgnoreCase(player.getUsername())) {
                    user.setUsername(player.getUsername());
                    updateUser = true;
                }
            }
            if (updateUser) user.update();
            Punished punished = plugin.getBackend().getPunishmentManager().getPunished(uuid, 10000).orElse(null);
            if (punished == null) {
                punished = new Punished(uuid);
                punished.update();
            }
        });
    }
}
