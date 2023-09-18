package com.uroria.backend.service.modules.clan;

import com.uroria.backend.impl.pulsarold.PulsarUpdate;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ClanUpdate extends PulsarUpdate<ClanOld> {
    private final BackendClanManager clanManager;

    public ClanUpdate(@NonNull PulsarClient pulsarClient, BackendClanManager clanManager) throws PulsarClientException {
        super(pulsarClient, "clan:update", clanManager.getModuleName());
        this.clanManager = clanManager;
    }

    @Override
    protected void onUpdate(ClanOld clan) {
        this.clanManager.updateDatabase(clan);
    }
}
