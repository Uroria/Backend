package com.uroria.backend.service.modules.clan;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.pulsar.PulsarUpdate;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ClanUpdate extends PulsarUpdate<Clan> {
    private final BackendClanManager clanManager;

    public ClanUpdate(@NonNull PulsarClient pulsarClient, BackendClanManager clanManager) throws PulsarClientException {
        super(pulsarClient, "clan:update", clanManager.getModuleName());
        this.clanManager = clanManager;
    }

    @Override
    protected void onUpdate(Clan clan) {
        this.clanManager.updateDatabase(clan);
    }
}
