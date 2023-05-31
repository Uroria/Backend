package com.uroria.backend.velocity;

import com.uroria.backend.StatsManager;
import com.uroria.backend.common.BackendStat;
import com.uroria.backend.scheduler.BackendScheduler;
import com.velocitypowered.api.proxy.ProxyServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class StatsManagerImpl extends StatsManager {
    private final ProxyServer proxyServer;
    private final int keepAlive = BackendVelocityPlugin.getConfig().getOrSetDefault("cacheKeepAliveInMinutes.stats", 10);

    StatsManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
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

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            Collection<UUID> markedForRemoval = new ArrayList<>();
            for (BackendStat stat : this.stats) {
                if (this.proxyServer.getPlayer(stat.getUUID()).isEmpty()) markedForRemoval.add(stat.getUUID());
            }
            return markedForRemoval;
        }, this.keepAlive, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.stats.removeIf(stat -> stat.getUUID().equals(uuid));
            }
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception", throwable);
            BackendAPI.captureException(throwable);
            runCacheChecker();
        });
    }
}
