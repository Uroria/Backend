package com.uroria.backend.velocity.permission;

import com.uroria.backend.impl.permission.AbstractPermManager;
import com.uroria.backend.impl.permission.group.GroupNameRequestChannel;
import com.uroria.backend.impl.permission.group.GroupUpdateChannel;
import com.uroria.backend.impl.permission.holder.HolderUUIDRequestChannel;
import com.uroria.backend.impl.permission.holder.HolderUpdateChannel;
import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.permission.PermHolder;
import com.uroria.backend.permission.PermManager;
import com.uroria.backend.velocity.BackendVelocityPlugin;
import com.velocitypowered.api.proxy.ProxyServer;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class PermManagerImpl extends AbstractPermManager implements PermManager {
    private final ProxyServer proxyServer;
    private GroupNameRequestChannel groupRequest;
    private GroupUpdateChannel groupUpdate;
    private HolderUUIDRequestChannel holderRequest;
    private HolderUpdateChannel holderUpdate;

    public PermManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.groupRequest = new GroupNameRequestChannel(this.pulsarClient, identifier);
        this.groupUpdate = new GroupUpdateChannel(this.pulsarClient, identifier, this::checkGroup);
        this.holderRequest = new HolderUUIDRequestChannel(this.pulsarClient, identifier);
        this.holderUpdate = new HolderUpdateChannel(this.pulsarClient, identifier, this::checkHolder);
        runCacheChecker();
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.groupRequest != null) this.groupRequest.close();
        if (this.groupUpdate != null) this.groupUpdate.close();
        if (this.holderRequest != null) this.holderRequest.close();
        if (this.holderUpdate != null) this.holderUpdate.close();
    }

    @Override
    protected void checkGroup(@NonNull PermGroup group) {
        if (this.groups.stream().noneMatch(group::equals)) return;

        if (group.isDeleted()) {
            this.groups.remove(group);
            this.proxyServer.getEventManager().fireAndForget(new GroupUpdateEvent(group));
            return;
        }

        for (PermGroup cachedGroup : this.groups) {
            if (!cachedGroup.equals(group)) continue;
            cachedGroup.modify(group);

            logger.info("Updated " + group);

            this.proxyServer.getEventManager().fireAndForget(new GroupUpdateEvent(cachedGroup));
            return;
        }

        logger.info("Adding " + group);
        this.groups.add(group);
        this.proxyServer.getEventManager().fireAndForget(new GroupUpdateEvent(group));
    }

    @Override
    protected void checkHolder(@NonNull PermHolder holder) {
        if (this.holders.stream().noneMatch(holder::equals)) return;

        if (holder.isDeleted()) {
            this.holders.remove(holder);
            this.proxyServer.getEventManager().fireAndForget(new HolderUpdateEvent(holder));
            return;
        }

        for (PermHolder cachedHolder : this.holders) {
            if (!cachedHolder.equals(holder)) continue;
            cachedHolder.modify(holder);

            logger.info("Updated " + holder);

            this.proxyServer.getEventManager().fireAndForget(new HolderUpdateEvent(cachedHolder));
            return;
        }

        logger.info("Adding " + holder);
        this.holders.add(holder);
        this.proxyServer.getEventManager().fireAndForget(new HolderUpdateEvent(holder));
    }

    @Override
    public Optional<PermHolder> getHolder(UUID uuid, int timeout) {
        for (PermHolder holder : this.holders) {
            if (holder.getUUID().equals(uuid)) return Optional.of(holder);
        }

        if (BackendVelocityPlugin.isOffline()) return Optional.empty();

        Optional<PermHolder> request = this.holderRequest.request(uuid, timeout);
        request.ifPresent(this.holders::add);
        return request;
    }

    @Override
    public Optional<PermGroup> getGroup(String name, int timeout) {
        for (PermGroup group : this.groups) {
            if (group.getName().equals(name)) return Optional.of(group);
        }

        if (BackendVelocityPlugin.isOffline()) return Optional.empty();

        Optional<PermGroup> request = this.groupRequest.request(name, timeout);
        request.ifPresent(this.groups::add);
        return request;
    }

    @Override
    public void updateHolder(@NonNull PermHolder holder) {
        try {
            checkHolder(holder);
            if (BackendVelocityPlugin.isOffline()) return;
            this.holderUpdate.update(holder);
        } catch (Exception exception) {
            logger.error("Cannot update " + holder, exception);
        }
    }

    @Override
    public void updateGroup(@NonNull PermGroup group) {
        try {
            checkGroup(group);
            if (BackendVelocityPlugin.isOffline()) return;
            this.groupUpdate.update(group);
        } catch (Exception exception) {
            logger.error("Cannot update " + group, exception);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            ObjectArraySet<UUID> markedForRemoval = new ObjectArraySet<>();
            for (PermHolder holder : this.holders) {
                if (this.proxyServer.getPlayer(holder.getUUID()).isEmpty()) markedForRemoval.add(holder.getUUID());
            }
            return markedForRemoval;
        }, 10, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.holders.removeIf(user -> user.getUUID().equals(uuid));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " Users flushed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception in cache checker", throwable);
            runCacheChecker();
        });
    }
}
