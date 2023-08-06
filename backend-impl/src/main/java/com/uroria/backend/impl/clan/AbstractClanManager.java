package com.uroria.backend.impl.clan;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.clan.ClanManager;
import com.uroria.backend.impl.AbstractManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

public abstract class AbstractClanManager extends AbstractManager implements ClanManager {
    protected final ObjectArraySet<Clan> clans;

    public AbstractClanManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.clans = new ObjectArraySet<>();
    }

    abstract protected void checkClan(@NonNull Clan clan);
}
