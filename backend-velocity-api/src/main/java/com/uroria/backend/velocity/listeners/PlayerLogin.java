package com.uroria.backend.velocity.listeners;

import com.uroria.backend.common.player.PlayerManager;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;

import java.util.UUID;

public record PlayerLogin(PlayerManager playerManager) {

    @Subscribe (order = PostOrder.FIRST)
    public EventTask onPlayerPreLoginEvent(LoginEvent loginEvent) {
        return EventTask.async(() -> {
            UUID uuid = loginEvent.getPlayer().getUniqueId();
            this.playerManager.getPlayer(uuid);
        });
    }
}
