package com.uroria.backend.impl.clan;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.clan.ClanManager;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractClanManager extends AbstractManager implements ClanManager {
    protected final Logger logger;
    protected final Collection<BackendClan> clans;

    public AbstractClanManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.clans = new CopyOnWriteArrayList<>();
    }

    abstract protected void checkClan(BackendClan clan);

    @Override
    public Optional<BackendClan> getClan(@NonNull String tag) {
        return getClan(tag, 5000);
    }

    @Override
    public Optional<BackendClan> getClan(@NonNull UUID operator) {
        return getClan(operator, 5000);
    }
}
