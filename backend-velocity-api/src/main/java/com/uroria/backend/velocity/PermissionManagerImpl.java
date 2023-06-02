package com.uroria.backend.velocity;

import com.uroria.backend.permission.PermissionManager;
import com.uroria.backend.common.PermissionGroup;
import com.uroria.backend.common.PermissionHolder;
import com.uroria.backend.scheduler.BackendScheduler;
import com.velocitypowered.api.proxy.ProxyServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PermissionManagerImpl extends PermissionManager {
    private final ProxyServer proxyServer;
    private final int keepAlive = BackendVelocityPlugin.getConfig().getOrSetDefault("cacheKeepAliveInMinutes.permission_holder", 30);

    PermissionManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
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
    public Optional<PermissionHolder> getPermissionHolder(UUID uuid, int timeout) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        return Optional.empty();
    }

    @Override
    public Optional<PermissionGroup> getPermissionGroup(String name, int timeout) {
        if (name == null) throw new NullPointerException("Name cannot be null");
        return Optional.empty();
    }

    @Override
    public void updatePermissionHolder(PermissionHolder permissionHolder) {
        if (permissionHolder == null) throw new NullPointerException("PermissionHolder cannot be null");
    }

    @Override
    public void updatePermissionGroup(PermissionGroup permissionGroup) {
        if (permissionGroup == null) throw new NullPointerException("PermissionGroup cannot be null");
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            Collection<UUID> markedForRemoval = new ArrayList<>();
            for (PermissionHolder holder : this.holders) {
                if (this.proxyServer.getPlayer(holder.getUUID()).isEmpty()) markedForRemoval.add(holder.getUUID());
            }
            return markedForRemoval;
        }, keepAlive, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.holders.removeIf(holder -> holder.getUUID().equals(uuid));
            }
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception", throwable);
            BackendAPI.captureException(throwable);
            runCacheChecker();
        });
    }
}
