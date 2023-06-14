package com.uroria.backend.bukkit;

import com.uroria.backend.bukkit.events.PermissionGroupUpdateEvent;
import com.uroria.backend.bukkit.events.PermissionHolderUpdateEvent;
import com.uroria.backend.common.PermissionGroup;
import com.uroria.backend.common.PermissionHolder;
import com.uroria.backend.permission.BackendPermissionGroupRequest;
import com.uroria.backend.permission.BackendPermissionGroupUpdate;
import com.uroria.backend.permission.BackendPermissionHolderRequest;
import com.uroria.backend.permission.BackendPermissionHolderUpdate;
import com.uroria.backend.permission.PermissionManager;
import com.uroria.backend.scheduler.BackendScheduler;
import org.apache.pulsar.client.api.PulsarClient;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class PermissionManagerImpl extends PermissionManager {
    private final int keepAlive = BackendBukkitPlugin.config().getOrSetDefault("cacheKeepAliveInMinutes.permission_holder", 30);
    private BackendPermissionGroupRequest groupRequest;
    private BackendPermissionHolderRequest holderRequest;
    private BackendPermissionGroupUpdate groupUpdate;
    private BackendPermissionHolderUpdate holderUpdate;

    PermissionManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
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
            BackendAPI.captureException(exception);
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
            BackendAPI.captureException(exception);
        }
    }

    @Override
    protected void checkPermissionHolder(PermissionHolder holder) {
        if (this.holders.stream().noneMatch(holder1 -> holder1.getUUID().equals(holder.getUUID()))) return;
        this.holders.removeIf(holder1 ->  holder1.getUUID().equals(holder.getUUID()));
        this.holders.add(holder);
        CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new PermissionHolderUpdateEvent(holder)));
    }

    @Override
    protected void checkPermissionGroup(PermissionGroup group) {
        if (this.groups.stream().noneMatch(group1 -> group1.getName().equals(group.getName()))) return;
        this.groups.removeIf(group1 -> group1.getName().equals(group.getName()));
        this.groups.add(group);
        CompletableFuture.runAsync(() -> Bukkit.getPluginManager().callEvent(new PermissionGroupUpdateEvent(group)));
    }

    @Override
    public Optional<PermissionHolder> getPermissionHolder(UUID uuid, int timeout) {
        if (uuid == null) throw new NullPointerException("UUID cannot be null");
        for (PermissionHolder holder : this.holders) {
            if (holder.getUUID().equals(uuid)) return Optional.of(holder);
        }
        Optional<PermissionHolder> request = holderRequest.request(uuid);
        request.ifPresent(holders::add);
        return request;
    }

    @Override
    public Optional<PermissionGroup> getPermissionGroup(String name, int timeout) {
        if (name == null) throw new NullPointerException("Name cannot be null");
        name = name.toLowerCase();
        for (PermissionGroup group : this.groups) {
            if (group.getName().equals(name)) return Optional.of(group);
        }
        Optional<PermissionGroup> request = groupRequest.request(name);
        request.ifPresent(groups::add);
        return request;
    }

    @Override
    public void updatePermissionHolder(PermissionHolder permissionHolder) {
        if (permissionHolder == null) throw new NullPointerException("PermissionHolder cannot be null");
        try {
            checkPermissionHolder(permissionHolder);
            this.holderUpdate.update(permissionHolder);
        } catch (Exception exception) {
            this.logger.error("Cannot update permissionholder", exception);
            BackendAPI.captureException(exception);
        }
    }

    @Override
    public void updatePermissionGroup(PermissionGroup permissionGroup) {
        if (permissionGroup == null) throw new NullPointerException("PermissionGroup cannot be null");
        try {
            checkPermissionGroup(permissionGroup);
            this.groupUpdate.update(permissionGroup);
        } catch (Exception exception) {
            this.logger.error("Cannot update permissiongroup", exception);
            BackendAPI.captureException(exception);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            Collection<UUID> markedForRemoval = new ArrayList<>();
            for (PermissionHolder holder : this.holders) {
                if (Bukkit.getPlayer(holder.getUUID()) == null) markedForRemoval.add(holder.getUUID());
            }
            return markedForRemoval;
        }, keepAlive, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.holders.removeIf(holder -> holder.getUUID().equals(uuid));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " permission-holders removed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception", throwable);
            BackendAPI.captureException(throwable);
            runCacheChecker();
        });
    }
}
