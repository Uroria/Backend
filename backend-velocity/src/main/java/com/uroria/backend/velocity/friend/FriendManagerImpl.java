package com.uroria.backend.velocity.friend;

import com.uroria.backend.friend.FriendHolder;
import com.uroria.backend.friend.FriendManager;
import com.uroria.backend.impl.friend.AbstractFriendManager;
import com.uroria.backend.impl.friend.FriendUUIDRequestChannel;
import com.uroria.backend.impl.friend.FriendUpdateChannel;
import com.uroria.backend.impl.scheduler.BackendScheduler;
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

public final class FriendManagerImpl extends AbstractFriendManager implements FriendManager {
    private final ProxyServer proxyServer;
    private FriendUUIDRequestChannel request;
    private FriendUpdateChannel update;

    public FriendManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
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
            this.proxyServer.getEventManager().fireAndForget(new FriendUpdateEvent(holder));
            return;
        }

        for (FriendHolder cachedHolder : this.holders) {
            if (!cachedHolder.equals(holder)) continue;
            cachedHolder.modify(holder);

            logger.info("Updated " + holder);

            this.proxyServer.getEventManager().fireAndForget(new FriendUpdateEvent(cachedHolder));
            return;
        }

        logger.info("Adding " + holder);
        this.holders.add(holder);
        this.proxyServer.getEventManager().fireAndForget(new FriendUpdateEvent(holder));
    }

    @Override
    public Optional<FriendHolder> getFriendHolder(UUID uuid, int timeout) {
        for (FriendHolder holder : this.holders) {
            if (holder.getUUID().equals(uuid)) return Optional.of(holder);
        }

        if (BackendVelocityPlugin.isOffline()) return Optional.empty();

        Optional<FriendHolder> request = this.request.request(uuid, timeout);
        request.ifPresent(this.holders::add);
        return request;
    }

    @Override
    public void updateFriendHolder(@NonNull FriendHolder holder) {
        try {
            checkFriend(holder);
            if (BackendVelocityPlugin.isOffline()) return;
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
                if (this.proxyServer.getPlayer(uuid).isEmpty()) markedForRemoval.add(uuid);
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
