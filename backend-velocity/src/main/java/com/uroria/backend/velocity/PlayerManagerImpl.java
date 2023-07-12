package com.uroria.backend.velocity;

import com.uroria.backend.impl.player.AbstractPlayerManager;
import com.uroria.backend.impl.player.BackendPlayerNameRequest;
import com.uroria.backend.impl.player.BackendPlayerUUIDRequest;
import com.uroria.backend.impl.player.BackendPlayerUpdate;
import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.backend.player.BackendPlayer;
import com.uroria.backend.velocity.events.PlayerUpdateEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PlayerManagerImpl extends AbstractPlayerManager {
    private final ProxyServer proxyServer;
    private final int keepAlive = BackendVelocityPlugin.getConfig().getOrSetDefault("cacheKeepAliveInMinutes.player", 20);
    private BackendPlayerUUIDRequest uuidRequest;
    private BackendPlayerNameRequest nameRequest;
    private BackendPlayerUpdate update;

    PlayerManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    protected void start(String identifier) {
        runCacheChecker();
        try {
            this.uuidRequest = new BackendPlayerUUIDRequest(this.pulsarClient, identifier);
            this.nameRequest = new BackendPlayerNameRequest(this.pulsarClient, identifier);
            this.update = new BackendPlayerUpdate(this.pulsarClient, identifier, this::checkPlayer);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void shutdown() {
        try {
            if (this.uuidRequest != null) this.uuidRequest.close();
            if (this.nameRequest != null) this.nameRequest.close();
            if (this.update != null) this.update.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void checkPlayer(BackendPlayer player) {
        if (this.players.stream().noneMatch(player1 -> player1.getUUID().equals(player.getUUID()))) return;

        for (BackendPlayer savedPlayer : this.players) {
            if (!savedPlayer.equals(player)) continue;
            savedPlayer.modify(player);

            logger.info("Updating player " + player.getUUID() + ":" + player.getCurrentName().orElse(null));

            this.proxyServer.getEventManager().fireAndForget(new PlayerUpdateEvent(savedPlayer));
            return;
        }

        logger.info("Adding player " + player.getUUID() + ":" + player.getCurrentName().orElse(null));
        this.players.add(player);
        this.proxyServer.getEventManager().fireAndForget(new PlayerUpdateEvent(player));
    }

    @Override
    public Optional<BackendPlayer> getPlayer(@NotNull UUID uuid, int timeout) {
        for (BackendPlayer player : this.players) {
            if (player.getUUID().equals(uuid)) return Optional.of(player);
        }
        Optional<BackendPlayer> request = uuidRequest.request(uuid, timeout);
        request.ifPresent(players::add);
        return request;
    }

    @Override
    public Optional<BackendPlayer> getPlayer(@NotNull String name, int timeout) {
        name = name.toLowerCase();
        for (BackendPlayer player : this.players) {
            if (player.getCurrentName().isPresent() && player.getCurrentName().get().equals(name)) return Optional.of(player);
        }

        Optional<BackendPlayer> request = nameRequest.request(name, timeout);
        request.ifPresent(players::add);
        return request;
    }

    @Override
    public BackendPlayer updatePlayer(@NotNull BackendPlayer player) {
        try {
            checkPlayer(player);
            this.update.update(player);
        } catch (Exception exception) {
            this.logger.error("Cannot update player", exception);
            BackendAPIImpl.captureException(exception);
        }
        return player;
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
            BackendAPIImpl.captureException(throwable);
            runCacheChecker();
        });
    }
}
