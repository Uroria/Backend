package com.uroria.backend.wrapper.permission;

import com.uroria.backend.impl.permission.AbstractPermManager;
import com.uroria.backend.impl.permission.group.GroupNameRequestChannel;
import com.uroria.backend.impl.permission.group.GroupUpdateChannel;
import com.uroria.backend.impl.permission.holder.HolderUUIDRequestChannel;
import com.uroria.backend.impl.permission.holder.HolderUpdateChannel;
import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.base.event.EventManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public final class PermManagerImpl extends AbstractPermManager implements PermManager {
    private final Function<UUID, Boolean> onlinePlayerCheck;
    private final boolean offline;
    private final EventManager eventManager;
    private GroupNameRequestChannel groupRequest;
    private GroupUpdateChannel groupUpdate;
    private HolderUUIDRequestChannel holderRequest;
    private HolderUpdateChannel holderUpdate;

    public PermManagerImpl(PulsarClient pulsarClient, Logger logger, Function<UUID, Boolean> onlinePlayerCheck, boolean offline, EventManager eventManager) {
        super(pulsarClient, logger);
        this.onlinePlayerCheck = onlinePlayerCheck;
        this.offline = offline;
        this.eventManager = eventManager;
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
    protected void checkGroup(@NonNull PermGroupOld group) {
        if (this.groups.stream().noneMatch(group::equals)) return;

        if (group.isDeleted()) {
            this.groups.remove(group);
            this.eventManager.callAndForget(new GroupUpdateEvent(group));
            return;
        }

        for (PermGroupOld cachedGroup : this.groups) {
            if (!cachedGroup.equals(group)) continue;
            cachedGroup.modify(group);

            logger.info("Updated " + group);

            this.eventManager.callAndForget(new GroupUpdateEvent(cachedGroup));
            return;
        }

        logger.info("Adding " + group);
        this.groups.add(group);
        this.eventManager.callAndForget(new GroupUpdateEvent(group));
    }

    @Override
    protected void checkHolder(@NonNull PermHolderOld holder) {
        if (this.holders.stream().noneMatch(holder::equals)) return;

        if (holder.isDeleted()) {
            this.holders.remove(holder);
            this.eventManager.callAndForget(new HolderUpdateEvent(holder));
            return;
        }

        for (PermHolderOld cachedHolder : this.holders) {
            if (!cachedHolder.equals(holder)) continue;
            cachedHolder.modify(holder);

            logger.info("Updated " + holder);

            this.eventManager.callAndForget(new HolderUpdateEvent(cachedHolder));
            return;
        }

        logger.info("Adding " + holder);
        this.holders.add(holder);
        this.eventManager.callAndForget(new HolderUpdateEvent(holder));
    }

    @Override
    public Optional<PermHolderOld> getHolder(UUID uuid, int timeout) {
        for (PermHolderOld holder : this.holders) {
            if (holder.getUUID().equals(uuid)) return Optional.of(holder);
        }

        if (this.offline) return Optional.of(new PermHolderOld(uuid));

        Optional<PermHolderOld> request = this.holderRequest.request(uuid, timeout);
        request.ifPresent(this.holders::add);
        return request;
    }

    @Override
    public Optional<PermGroupOld> getGroup(String name, int timeout) {
        for (PermGroupOld group : this.groups) {
            if (group.getName().equals(name)) return Optional.of(group);
        }

        if (this.offline) return Optional.empty();

        Optional<PermGroupOld> request = this.groupRequest.request(name, timeout);
        request.ifPresent(this.groups::add);
        return request;
    }

    @Override
    public void updateHolder(@NonNull PermHolderOld holder) {
        if (this.holders.stream().noneMatch(holder::equals)) this.holders.add(holder);
        try {
            checkHolder(holder);
            if (this.offline) return;
            this.holderUpdate.update(holder);
        } catch (Exception exception) {
            logger.error("Cannot update " + holder, exception);
        }
    }

    @Override
    public void updateGroup(@NonNull PermGroupOld group) {
        try {
            checkGroup(group);
            if (this.offline) return;
            this.groupUpdate.update(group);
        } catch (Exception exception) {
            logger.error("Cannot update " + group, exception);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            ObjectArraySet<UUID> markedForRemoval = new ObjectArraySet<>();
            for (PermHolderOld holder : this.holders) {
                if (!this.onlinePlayerCheck.apply(holder.getUUID())) markedForRemoval.add(holder.getUUID());
            }
            return markedForRemoval;
        }, 10, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.holders.removeIf(user -> user.getUUID().equals(uuid));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " PermHolders flushed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception in cache checker", throwable);
            runCacheChecker();
        });
    }
}
