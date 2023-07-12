package com.uroria.backend.server.modules.clan;

import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendClanUpdate extends PulsarUpdate<BackendClan> {
    private final BackendClanManager clanManager;

    public BackendClanUpdate(PulsarClient pulsarClient, BackendClanManager clanManager) throws PulsarClientException {
        super(pulsarClient, "clan:update", "ClanModule");
        this.clanManager = clanManager;
    }

    @Override
    protected void onUpdate(BackendClan clan) {
        this.clanManager.updateLocal(clan);
    }
}
