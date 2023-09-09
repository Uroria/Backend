package com.uroria.backend.wrapper.punishment;

import com.uroria.backend.impl.punishment.AbstractPunishmentManager;
import com.uroria.backend.impl.punishment.PunishedRequestChannel;
import com.uroria.backend.impl.punishment.PunishedUpdateChannel;
import com.uroria.backend.impl.scheduler.BackendScheduler;
import com.uroria.backend.punishment.Punished;
import com.uroria.backend.punishment.PunishmentManager;
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

public final class PunishmentManagerImpl extends AbstractPunishmentManager implements PunishmentManager {
    private final Function<UUID, Boolean> onlinePlayerCheck;
    private final boolean offline;
    private final EventManager eventManager;
    private PunishedRequestChannel request;
    private PunishedUpdateChannel update;

    public PunishmentManagerImpl(PulsarClient pulsarClient, Logger logger, Function<UUID, Boolean> onlinePlayerCheck, boolean offline, EventManager eventManager) {
        super(pulsarClient, logger);
        this.onlinePlayerCheck = onlinePlayerCheck;
        this.offline = offline;
        this.eventManager = eventManager;
    }

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.request = new PunishedRequestChannel(this.pulsarClient, identifier);
        this.update = new PunishedUpdateChannel(this.pulsarClient, identifier, this::checkPunished);
        runCacheChecker();
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.request != null) this.request.close();
        if (this.update != null) this.update.close();
    }

    @Override
    protected void checkPunished(@NonNull Punished punished) {
        if (this.punisheds.stream().noneMatch(punished::equals)) return;

        if (punished.isDeleted()) {
            this.punisheds.remove(punished);
            this.eventManager.callAndForget(new PunishedUpdateEvent(punished));
            return;
        }

        for (Punished cachedPunished : this.punisheds) {
            if (!cachedPunished.equals(punished)) continue;
            cachedPunished.modify(punished);

            logger.info("Updated " + punished);

            this.eventManager.callAndForget(new PunishedUpdateEvent(cachedPunished));
            return;
        }

        logger.info("Adding " + punished);
        this.punisheds.add(punished);
        this.eventManager.callAndForget(new PunishedUpdateEvent(punished));
    }

    @Override
    public Optional<Punished> getPunished(UUID uuid, int timeout) {
        for (Punished punished : this.punisheds) {
            if (punished.getUUID().equals(uuid)) return Optional.of(punished);
        }

        if (this.offline) return Optional.empty();

        Optional<Punished> request = this.request.request(uuid, timeout);
        request.ifPresent(this.punisheds::add);
        return request;
    }

    @Override
    public void updatePunished(@NonNull Punished punished) {
        if (this.punisheds.stream().noneMatch(punished::equals)) this.punisheds.add(punished);
        try {
            checkPunished(punished);
            if (this.offline) return;
            this.update.update(punished);
        } catch (Exception exception) {
            this.logger.error("Cannot update " + punished);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            ObjectArraySet<UUID> markedForRemoval = new ObjectArraySet<>();
            for (Punished punished : this.punisheds) {
                if (!this.onlinePlayerCheck.apply(punished.getUUID())) markedForRemoval.add(punished.getUUID());
            }
            return markedForRemoval;
        }, 20, TimeUnit.MINUTES).run(markedForRemoval -> {
            for (UUID uuid : markedForRemoval) {
                this.punisheds.removeIf(punished -> punished.getUUID().equals(uuid));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " Punisheds flushed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception in cache checker", throwable);
            runCacheChecker();
        });
    }
}
