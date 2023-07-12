package com.uroria.backend.server.modules.clan;

import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class BackendClanOperatorResponse extends PulsarResponse<UUID, BackendClan> {
    private final BackendClanManager clanManager;

    public BackendClanOperatorResponse(PulsarClient pulsarClient, BackendClanManager clanManager) throws PulsarClientException {
        super(pulsarClient, "clan:request:operator", "clan:response:operator", "ClanModule");
        this.clanManager = clanManager;
    }

    @Override
    protected BackendClan response(UUID key) {
        return this.clanManager.getClan(key).orElse(null);
    }
}
