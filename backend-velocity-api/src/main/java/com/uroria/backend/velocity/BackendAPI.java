package com.uroria.backend.velocity;

import com.uroria.backend.AbstractBackendAPI;
import com.uroria.backend.PermissionManager;
import com.uroria.backend.PlayerManager;
import com.velocitypowered.api.proxy.ProxyServer;
import io.sentry.Sentry;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public final class BackendAPI extends AbstractBackendAPI {
    private static BackendAPI instance;
    private final boolean sentry;
    private final Logger logger;
    private final ProxyServer proxyServer;
    private final PlayerManagerImpl playerManager;
    private final PermissionManagerImpl permissionManager;
    BackendAPI(String pulsarURL, boolean sentry, Logger logger, ProxyServer proxyServer) {
        super(pulsarURL);
        instance = this;
        this.sentry = sentry;
        this.logger = logger;
        this.proxyServer = proxyServer;
        this.playerManager = new PlayerManagerImpl(this.pulsarClient, this.logger, proxyServer);
        this.permissionManager = new PermissionManagerImpl(this.pulsarClient, this.logger, this.proxyServer);
    }

    @Override
    protected void start() {
        this.logger.info("Starting connections...");

    }

    @Override
    protected void shutdown() {
        this.logger.info("Shutting down connections...");
        try {

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

    public static BackendAPI getAPI() {
        return instance;
    }
}
