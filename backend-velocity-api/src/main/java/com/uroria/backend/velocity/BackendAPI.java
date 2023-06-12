package com.uroria.backend.velocity;

import com.uroria.backend.AbstractBackendAPI;
import com.uroria.backend.permission.PermissionManager;
import com.uroria.backend.player.PlayerManager;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.stats.StatsManager;
import com.velocitypowered.api.proxy.ProxyServer;
import io.sentry.Sentry;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BackendAPI extends AbstractBackendAPI {
    private static BackendAPI instance;
    private final boolean sentry;
    private final Logger logger;
    private final ProxyServer proxyServer;
    private final PlayerManagerImpl playerManager;
    private final PermissionManagerImpl permissionManager;
    private final StatsManagerImpl statsManager;
    private final ServerManagerImpl serverManager;

    BackendAPI(String pulsarURL, boolean sentry, Logger logger, ProxyServer proxyServer) {
        super(pulsarURL);
        instance = this;
        this.sentry = sentry;
        this.logger = logger;
        this.proxyServer = proxyServer;
        this.playerManager = new PlayerManagerImpl(this.pulsarClient, this.logger, proxyServer);
        this.permissionManager = new PermissionManagerImpl(this.pulsarClient, this.logger, this.proxyServer);
        this.statsManager = new StatsManagerImpl(this.pulsarClient, this.logger, this.proxyServer);
        this.serverManager = new ServerManagerImpl(this.pulsarClient, this.logger, this.proxyServer);
    }

    @Override
    protected void start() {
        String identifier = UUID.randomUUID().toString();
        this.logger.info("Starting connections...");
        try {
            this.playerManager.start(identifier);
            this.permissionManager.start(identifier);
            this.statsManager.start(identifier);
            this.serverManager.start(identifier);
        } catch (Exception exception) {
            this.logger.error("Cannot start modules", exception);
            captureException(exception);
        }
    }

    @Override
    protected void shutdown() {
        this.logger.info("Shutting down connections...");
        try {
            this.playerManager.shutdown();
            this.permissionManager.shutdown();
            this.serverManager.shutdown();
            this.statsManager.shutdown();
            super.shutdown();
        } catch (Exception exception) {
            this.logger.error("Cannot shutdown pulsar instances", exception);
        }
    }

    public static void captureException(Throwable throwable) {
        CompletableFuture.runAsync(() -> {
            if (!instance.sentry) return;
            Sentry.captureException(throwable, scope -> {
                scope.setTag("Service", "Velocity");

                if (instance != null) {
                    scope.setContexts("Instance", "Initialized");

                    scope.setContexts("Velocity.PlayerCount", instance.proxyServer.getPlayerCount());
                    scope.setContexts("Velocity.ServerCount", instance.proxyServer.getAllServers().size());
                    scope.setContexts("Velocity.Version", instance.proxyServer.getVersion().getVersion());
                } else {
                    scope.setContexts("Instance", "Not initialized");
                }

                Thread thread = Thread.currentThread();
                scope.setContexts("Thread.Name", thread.getName());
                scope.setContexts("Thread.Alive", thread.isAlive());
            });
        });
    }

    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    public ServerManager getServerManager() {
        return this.serverManager;
    }

    public static BackendAPI getAPI() {
        return instance;
    }
}
