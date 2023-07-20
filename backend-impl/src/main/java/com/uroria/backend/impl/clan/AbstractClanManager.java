package com.uroria.backend.impl.clan;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.clan.ClanManager;
import com.uroria.backend.utils.ObjectUtils;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public abstract class AbstractClanManager extends AbstractManager implements ClanManager {
    protected final Set<BackendClan> clans;

    public AbstractClanManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.clans = ObjectUtils.newSet();
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
