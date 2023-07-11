package com.uroria.backend.bukkit;

import com.uroria.backend.AbstractBackendAPI;
import com.uroria.backend.common.BackendAPI;
import com.uroria.backend.common.permission.PermissionManager;
import com.uroria.backend.common.player.PlayerManager;
import com.uroria.backend.common.settings.SettingsManager;
import com.uroria.backend.common.stats.StatsManager;
import com.uroria.backend.message.AbstractMessageManager;
import io.sentry.Sentry;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class BackendAPIImpl extends AbstractBackendAPI implements BackendAPI {
    private static BackendAPIImpl instance;
    private final boolean sentry;
    private final Logger logger;
    private final PlayerManagerImpl playerManager;
    private final PermissionManagerImpl permissionManager;
    private final StatsManagerImpl statsManager;
    private final ServerManagerImpl serverManager;
    private final MessageManagerImpl messageManager;
    private final SettingsManagerImpl settingsManager;
    BackendAPIImpl(String pulsarURL, boolean sentry, Logger logger) {
        super(pulsarURL);
        instance = this;
        this.sentry = sentry;
        this.logger = logger;
        this.playerManager = new PlayerManagerImpl(this.pulsarClient, this.logger);
        this.permissionManager = new PermissionManagerImpl(this.pulsarClient, this.logger);
        this.statsManager = new StatsManagerImpl(this.pulsarClient, this.logger);
        this.serverManager = new ServerManagerImpl(this.pulsarClient, this.logger);
        this.messageManager = new MessageManagerImpl(this.pulsarClient, this.logger);
        this.settingsManager = new SettingsManagerImpl(this.pulsarClient, this.logger);
    }

    @Override
    protected void start() {
        if (BackendBukkitPlugin.isOffline()) {
            logger.info("Running in offline mode!");
            return;
        }
        this.logger.info("Starting connections...");
        String identifier = UUID.randomUUID().toString();
        this.playerManager.start(identifier);
        this.permissionManager.start(identifier);
        this.statsManager.start(identifier);
        this.serverManager.start(identifier);
        this.messageManager.start(identifier);
        this.settingsManager.start(identifier);
    }

    @Override
    protected void shutdown() {
        if (BackendBukkitPlugin.isOffline()) return;
        this.logger.info("Shutting down connections...");
        try {
            this.playerManager.shutdown();
            this.permissionManager.shutdown();
            this.statsManager.shutdown();
            this.serverManager.shutdown();
            this.messageManager.shutdown();
            this.settingsManager.shutdown();
            super.shutdown();
        } catch (Exception exception) {
            this.logger.error("Cannot shutdown pulsar instances", exception);
        }
    }

    public static void captureException(Throwable throwable) {
        if (BackendBukkitPlugin.isOffline()) return;
        CompletableFuture.runAsync(() -> {
            if (!instance.sentry) return;
            Sentry.captureException(throwable, scope -> {
                scope.setTag("Service", "Bukkit");

                if (instance != null) {
                    scope.setContexts("Instance", "Initialized");

                    scope.setContexts("Bukkit.PlayerCount", Bukkit.getOnlinePlayers().size());
                    scope.setContexts("Bukkit.Version", Bukkit.getVersion());
                } else {
                    scope.setContexts("Instance", "Not initialized");
                }

                Thread thread = Thread.currentThread();
                scope.setContexts("Thread.Name", thread.getName());
                scope.setContexts("Thread.Alive", thread.isAlive());
            });
        });
    }

    public BukkitServerManager getServerManager() {
        return this.serverManager;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return this.playerManager;
    }

    @Override
    public PermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    @Override
    public StatsManager getStatsManager() {
        return this.statsManager;
    }

    public AbstractMessageManager getMessageManager() {
        return this.messageManager;
    }

    @Override
    public SettingsManager getSettingsManager() {
        return this.settingsManager;
    }

    public static BackendAPIImpl getAPI() {
        return instance;
    }
}
