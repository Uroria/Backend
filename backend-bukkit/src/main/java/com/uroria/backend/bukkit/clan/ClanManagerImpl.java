package com.uroria.backend.bukkit.clan;

import com.uroria.backend.bukkit.BackendBukkitPlugin;
import com.uroria.backend.bukkit.utils.BukkitUtils;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.clan.ClanManager;
import com.uroria.backend.impl.clan.AbstractClanManager;
import com.uroria.backend.impl.clan.ClanOperatorRequestChannel;
import com.uroria.backend.impl.clan.ClanTagRequestChannel;
import com.uroria.backend.impl.clan.ClanUpdateChannel;
import com.uroria.backend.impl.scheduler.BackendScheduler;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public final class ClanManagerImpl extends AbstractClanManager implements ClanManager {

    private ClanTagRequestChannel tagRequest;
    private ClanOperatorRequestChannel operatorRequest;
    private ClanUpdateChannel update;

    public ClanManagerImpl(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
    }

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.tagRequest = new ClanTagRequestChannel(this.pulsarClient, identifier);
        this.operatorRequest = new ClanOperatorRequestChannel(this.pulsarClient, identifier);
        this.update = new ClanUpdateChannel(this.pulsarClient, identifier, this::checkClan);
        runCacheChecker();
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.tagRequest != null) this.tagRequest.close();
        if (this.operatorRequest != null) this.operatorRequest.close();
        if (this.update != null) this.update.close();
    }

    @Override
    protected void checkClan(@NonNull Clan clan) {
        if (this.clans.stream().noneMatch(clan::equals)) return;

        if (clan.isDeleted()) {
            this.clans.remove(clan);
            BukkitUtils.callAsyncEvent(new ClanUpdateEvent(clan));
            return;
        }

        for (Clan cachedClan : this.clans) {
            if (!cachedClan.equals(clan)) continue;
            cachedClan.modify(clan);

            logger.info("Updated " + clan);

            BukkitUtils.callAsyncEvent(new ClanUpdateEvent(cachedClan));
            return;
        }

        logger.info("Adding " + clan);
        this.clans.add(clan);
        BukkitUtils.callAsyncEvent(new ClanUpdateEvent(clan));
    }

    @Override
    public Optional<Clan> getClan(String tag, int timeout) {
        for (Clan clan : this.clans) {
            if (clan.getTag().equals(tag)) return Optional.of(clan);
        }

        if (BackendBukkitPlugin.isOffline()) return Optional.empty();

        Optional<Clan> request = this.tagRequest.request(tag, timeout);
        request.ifPresent(this.clans::add);
        return request;
    }

    @Override
    public Optional<Clan> getClan(UUID operator, int timeout) {
        for (Clan clan : this.clans) {
            if (clan.getOperator().equals(operator)) return Optional.of(clan);
        }

        Optional<Clan> request = this.operatorRequest.request(operator, timeout);
        request.ifPresent(this.clans::add);
        return request;
    }

    @Override
    public void updateClan(@NonNull Clan clan) {
        try {
            checkClan(clan);
            if (BackendBukkitPlugin.isOffline()) return;
            this.update.update(clan);
        } catch (Exception exception) {
            logger.error("Cannot update " + clan, exception);
        }
    }

    private void runCacheChecker() {
        BackendScheduler.runTaskLater(() -> {
            ObjectArraySet<String> markedForRemoval = new ObjectArraySet<>();
            for (Clan clan : this.clans) {
                boolean remove = true;
                for (UUID uuid : clan.getMembers()) {
                    if (Bukkit.getPlayer(uuid) != null) remove = false;
                }
                if (remove) markedForRemoval.add(clan.getName());
            }
            return markedForRemoval;
        }, 2, TimeUnit.HOURS).run(markedForRemoval -> {
            for (String clanName : markedForRemoval) {
                this.clans.removeIf(clan -> clan.getName().equals(clanName));
            }
            int size = markedForRemoval.size();
            if (size > 0) this.logger.info(size + " Clans flushed from cache");
            runCacheChecker();
        }, throwable -> {
            this.logger.error("Unhandled exception in cache checker", throwable);
            runCacheChecker();
        });
    }
}
