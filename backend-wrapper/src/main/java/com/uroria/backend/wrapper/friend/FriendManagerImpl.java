package com.uroria.backend.wrapper.friend;

import com.uroria.backend.friend.FriendHolder;
import com.uroria.backend.friend.FriendManager;
import com.uroria.backend.impl.friend.AbstractFriendManager;
import com.uroria.backend.impl.friend.FriendUUIDRequestChannel;
import com.uroria.backend.impl.friend.FriendUpdateChannel;
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

public final class FriendManagerImpl extends AbstractFriendManager implements FriendManager {
    private final Function<UUID, Boolean> onlinePlayerCheck;
    private final boolean offline;
    private final EventManager eventManager;
    private FriendUUIDRequestChannel request;
    private FriendUpdateChannel update;

    public FriendManagerImpl(PulsarClient pulsarClient, Logger logger, Function<UUID, Boolean> onlinePlayerCheck, boolean offline, EventManager eventManager) {
        super(pulsarClient, logger);
        this.onlinePlayerCheck = onlinePlayerCheck;
        this.offline = offline;
        this.eventManager = eventManager;
    }

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.request = new FriendUUIDRequestChannel(this.pulsarClient, identifier);
        this.update = new FriendUpdateChannel(this.pulsarClient, identifier, this::checkFriend);
        runCacheChecker();
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.request != null) this.request.close();
        if (this.update != null) this.update.close();
    }

    @Override
    protected void checkFriend(@NonNull FriendHolder holder) {
        if (this.holders.stream().noneMatch(holder::equals)) return;

        if (holder.isDeleted()) {
            this.holders.remove(holder);
            this.eventManager.callAndForget(new FriendUpdateEvent(holder));
            return;
        }

        for (FriendHolder cachedHolder : this.holders) {
            if (!cachedHolder.equals(holder)) continue;
            cachedHolder.modify(holder);

            logger.info("Updated " + holder);

            this.eventManager.callAndForget(new FriendUpdateEvent(cachedHolder));
            return;
        }

        logger.info("Adding " + holder);
        this.holders.add(holder);
        this.eventManager.callAndForget(new FriendUpdateEvent(holder));
    }

    @Override
    public Optional<FriendHolder> getFriendHolder(UUID uuid, int timeout) {
        for (FriendHolder holder : this.holders) {
            if (holder.getUUID().equals(uuid)) return Optional.of(holder);
        }

        if (this.offline) return Optional.empty();

        Optional<FriendHolder> request = this.request.request(uuid, timeout);
        request.ifPresent(this.holders::add);
        return request;
    }

    @Override
    public void updateFriendHolder(@NonNull FriendHolder holder) {
        try {
            checkFriend(holder);
            if (this.offline) return;
            this.update.update(holder);
        } catch (Exception exception) {
            this.logger.error("Cannot update " + holder, exception);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            ObjectArraySet<UUID> markedForRemoval = new ObjectArraySet<>();
            for (FriendHolder holder : this.holders) {
                UUID uuid = holder.getUUID();
                if (!this.onlinePlayerCheck.apply(uuid)) markedForRemoval.add(uuid);
            }
            return markedForRemoval;
        }, 20, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.holders.removeIf(friend -> friend.getUUID().equals(uuid));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " Friends flushed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception in cache checker", throwable);
            runCacheChecker();
        });
    }
}
