package com.uroria.backend.velocity;

import com.uroria.backend.PlayerManager;
import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.scheduler.BackendScheduler;
import com.velocitypowered.api.proxy.ProxyServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PlayerManagerImpl extends PlayerManager {
    private final ProxyServer proxyServer;
    private final int keepAlive = BackendVelocityPlugin.getConfig().getOrSetDefault("cacheKeepAliveInMinutes.player", 10);

    PlayerManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    protected void start(String identifier) {
        runCacheChecker();
    }

    @Override
    protected void shutdown() {

    }

    @Override
    public Optional<BackendPlayer> getPlayer(UUID uuid) {
        for (BackendPlayer player : this.players) {
            if (player.getUUID().equals(uuid)) return Optional.of(player);
        }
        return Optional.empty();
    }

    @Override
    public Optional<BackendPlayer> getPlayer(String name) {
        for (BackendPlayer player : this.players) {
            if (player.getCurrentName().isPresent() && player.getCurrentName().get().equals(name)) return Optional.of(player);
        }
        return Optional.empty();
    }

    @Override
    public void updatePlayer(BackendPlayer player) {

    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            Collection<UUID> markedForRemoval = new ArrayList<>();
            for (BackendPlayer player : this.players) {
                if (this.proxyServer.getPlayer(player.getUUID()).isEmpty()) markedForRemoval.add(player.getUUID());
            }
            return markedForRemoval;
        }, keepAlive, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.players.removeIf(player -> player.getUUID().equals(uuid));
            }
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception", throwable);
            BackendAPI.captureException(throwable);
            runCacheChecker();
        });
    }
}
