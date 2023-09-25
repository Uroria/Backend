package com.uroria.backend.velocity.listeners;

import com.uroria.backend.Backend;
import com.uroria.backend.impl.user.UserWrapper;
import com.uroria.backend.velocity.BackendVelocityPlugin;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.UUID;

public record PlayerLogin(BackendVelocityPlugin plugin) {
    
    @SuppressWarnings("ConstantValue")
    @Subscribe (order = PostOrder.FIRST)
    public EventTask onPlayerLoginEvent(LoginEvent loginEvent) {
        return EventTask.async(() -> {
            Player player = loginEvent.getPlayer();
            UUID uuid = player.getUniqueId();

            Backend.getUser(uuid).ifPresentOrElse(user -> {
                try {
                    String username = user.getUsername();
                    if (username == null || !username.equals(player.getUsername())) {
                        user.setUsername(player.getUsername());
                    }
                    long currentTime = System.currentTimeMillis();
                    if (user.getFirstJoin() == 0) {
                        ((UserWrapper) user).setFirstJoin(currentTime);
                    }
                    user.setLastJoin(currentTime);
                } catch (Exception exception) {
                    loginEvent.setResult(ResultedEvent.ComponentResult.denied(MiniMessage.miniMessage().deserialize("<red>Unhandled exception in User resolving: " + exception.getMessage() + "</red>")));
                }
            }, () -> {
                loginEvent.setResult(ResultedEvent.ComponentResult.denied(MiniMessage.miniMessage().deserialize("<red>Unable to resolve User</red>")));
            });
        });
    }
}
