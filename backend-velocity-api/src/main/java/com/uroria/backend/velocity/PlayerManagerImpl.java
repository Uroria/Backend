package com.uroria.backend.velocity;

import com.uroria.backend.player.BackendPlayerNameRequest;
import com.uroria.backend.player.BackendPlayerUUIDRequest;
import com.uroria.backend.player.BackendPlayerUpdate;
import com.uroria.backend.player.PlayerManager;
import com.uroria.backend.common.BackendPlayer;
import com.uroria.backend.scheduler.BackendScheduler;
import com.uroria.backend.velocity.events.PlayerUpdateEvent;
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
            BackendAPI.captureException(exception);
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
            BackendAPI.captureException(exception);
        }
    }

    @Override
    protected void checkPlayer(BackendPlayer player) {
        if (this.players.stream().noneMatch(player1 -> player1.getUUID().equals(player.getUUID()))) return;
        this.players.removeIf(player1 -> player1.getUUID().equals(player.getUUID()));
        this.players.add(player);
        this.proxyServer.getEventManager().fireAndForget(new PlayerUpdateEvent(player));
    }

    @Override
    public Optional<BackendPlayer> getPlayer(UUID uuid, int timeout) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        for (BackendPlayer player : this.players) {
            if (player.getUUID().equals(uuid)) return Optional.of(player);
        }
        Optional<BackendPlayer> request = uuidRequest.request(uuid);
        request.ifPresent(this.players::add);
        return request;
    }

    @Override
    public Optional<BackendPlayer> getPlayer(String name, int timeout) {
        if (name == null) throw new NullPointerException("Name cannot be null");
        name = name.toLowerCase();
        for (BackendPlayer player : this.players) {
            if (player.getCurrentName().isPresent() && player.getCurrentName().get().equals(name)) return Optional.of(player);
        }

        Optional<BackendPlayer> request = nameRequest.request(name);
        request.ifPresent(this.players::add);
        return request;
    }

    @Override
    public void updatePlayer(BackendPlayer player) {
        if (player == null) throw new NullPointerException("Player cannot be null");
        try {
            checkPlayer(player);
            this.update.update(player);
        } catch (Exception exception) {
            this.logger.error("Cannot update player", exception);
            BackendAPI.captureException(exception);
        }
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
