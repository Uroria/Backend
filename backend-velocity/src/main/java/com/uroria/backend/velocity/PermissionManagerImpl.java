package com.uroria.backend.velocity;

import com.uroria.backend.impl.permission.*;
import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.backend.permission.PermissionGroup;
import com.uroria.backend.permission.PermissionHolder;
import com.uroria.backend.velocity.events.PermissionGroupUpdateEvent;
import com.uroria.backend.velocity.events.PermissionHolderUpdateEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PermissionManagerImpl extends AbstractPermissionManager {
    private final ProxyServer proxyServer;
    private final int keepAlive = BackendVelocityPlugin.getConfig().getOrSetDefault("cacheKeepAliveInMinutes.permission_holder", 30);
    private BackendPermissionGroupRequest groupRequest;
    private BackendPermissionHolderRequest holderRequest;
    private BackendPermissionGroupUpdate groupUpdate;
    private BackendPermissionHolderUpdate holderUpdate;

    PermissionManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    protected void start(String identifier) {
        runCacheChecker();
        try {
            this.groupRequest = new BackendPermissionGroupRequest(this.pulsarClient, identifier);
            this.holderRequest = new BackendPermissionHolderRequest(this.pulsarClient, identifier);
            this.groupUpdate = new BackendPermissionGroupUpdate(this.pulsarClient, identifier, this::checkPermissionGroup);
            this.holderUpdate = new BackendPermissionHolderUpdate(this.pulsarClient, identifier, this::checkPermissionHolder);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void shutdown() {
        try {
            if (this.groupRequest != null) this.groupRequest.close();
            if (this.holderRequest != null) this.holderRequest.close();
            if (this.groupUpdate != null) this.groupUpdate.close();
            if (this.holderUpdate != null) this.holderUpdate.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void checkPermissionHolder(PermissionHolder holder) {
        if (this.holders.stream().noneMatch(holder1 -> holder1.getUUID().equals(holder.getUUID()))) return;

        for (PermissionHolder savedHolder : this.holders) {
            if (!savedHolder.equals(holder)) continue;
            savedHolder.modify(holder);

            logger.info("Updating PermissionHolder " + holder.getUUID());

            this.proxyServer.getEventManager().fireAndForget(new PermissionHolderUpdateEvent(holder));
            return;
        }

        this.logger.info("Adding PermissionHolder " + holder.getUUID());
        this.holders.add(holder);
        this.proxyServer.getEventManager().fireAndForget(new PermissionHolderUpdateEvent(holder));
    }

    @Override
    protected void checkPermissionGroup(PermissionGroup group) {
        if (this.groups.stream().noneMatch(group1 -> group1.getName().equals(group.getName()))) return;

        for (PermissionGroup savedGroup : this.groups) {
            if (!savedGroup.equals(group)) continue;
            savedGroup.modify(group);

            this.logger.info("Updating PermissionGroup " + group.getName());

            this.proxyServer.getEventManager().fireAndForget(new PermissionGroupUpdateEvent(group));
            return;
        }

        this.logger.info("Adding PermissionGroup " + group.getName());
        this.groups.add(group);
        this.proxyServer.getEventManager().fireAndForget(new PermissionGroupUpdateEvent(group));
    }

    @Override
    public Optional<PermissionHolder> getHolder(@NonNull UUID uuid, int timeout) {
        for (PermissionHolder holder : this.holders) {
            if (holder.getUUID().equals(uuid)) return Optional.of(holder);
        }
        Optional<PermissionHolder> request = holderRequest.request(uuid, timeout);
        request.ifPresent(holders::add);
        return request;
    }

    @Override
    public Optional<PermissionGroup> getGroup(@NotNull String name, int timeout) {
        name = name.toLowerCase();
        for (PermissionGroup group : this.groups) {
            if (group.getName().equals(name)) return Optional.of(group);
        }
        Optional<PermissionGroup> request = groupRequest.request(name, timeout);
        request.ifPresent(groups::add);
        return request;
    }

    @Override
    public PermissionHolder updateHolder(@NotNull PermissionHolder permissionHolder) {
        try {
            checkPermissionHolder(permissionHolder);
            this.holderUpdate.update(permissionHolder);
        } catch (Exception exception) {
            this.logger.error("Cannot update permissionholder", exception);
            BackendAPIImpl.captureException(exception);
        }
        return permissionHolder;
    }

    @Override
    public PermissionGroup updateGroup(@NotNull PermissionGroup permissionGroup) {
        try {
            checkPermissionGroup(permissionGroup);
            this.groupUpdate.update(permissionGroup);
        } catch (Exception exception) {
            this.logger.error("Cannot update permission-group", exception);
            BackendAPIImpl.captureException(exception);
        }
        return permissionGroup;
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
            BackendAPIImpl.captureException(throwable);
            runCacheChecker();
        });
    }
}
