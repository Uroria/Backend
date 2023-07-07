package com.uroria.backend.clan;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.BackendClan;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class ClanManager extends AbstractManager {
    protected final Logger logger;
    protected final Collection<BackendClan> clans;

    public ClanManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.clans = new CopyOnWriteArrayList<>();
    }

    abstract protected void checkClan(BackendClan clan);

    abstract public Optional<BackendClan> getClan(String tag, int timout);

    abstract public Optional<BackendClan> getClan(UUID operator, int timeout);

    abstract public void updateClan(BackendClan clan);
}
