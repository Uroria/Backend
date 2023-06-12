package com.uroria.backend.bukkit;

import com.uroria.backend.common.BackendStat;
import com.uroria.backend.scheduler.BackendScheduler;
import com.uroria.backend.stats.StatsManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class StatsManagerImpl extends StatsManager {
    private final int keepAlive = BackendBukkitPlugin.config().getOrSetDefault("cacheKeepAliveInMinutes.stats", 10);

    StatsManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
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
                if (Bukkit.getPlayer(stat.getUUID()) == null) markedForRemoval.add(stat.getUUID());
            }
            return markedForRemoval;
        }, this.keepAlive, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.stats.removeIf(stat -> stat.getUUID().equals(uuid));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " stats removed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception", throwable);
            BackendAPI.captureException(throwable);
            runCacheChecker();
        });
    }
}
