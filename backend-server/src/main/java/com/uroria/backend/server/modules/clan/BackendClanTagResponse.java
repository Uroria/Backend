package com.uroria.backend.server.modules.clan;

import com.uroria.backend.clan.BackendClan;
import com.uroria.backend.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendClanTagResponse extends PulsarResponse<String, BackendClan> {
    private final BackendClanManager clanManager;

    public BackendClanTagResponse(PulsarClient pulsarClient, BackendClanManager clanManager) throws PulsarClientException {
        super(pulsarClient, "clan:request:tag", "clan:response:tag", "ClanModule");
        this.clanManager = clanManager;
    }

    @Override
    protected BackendClan response(String key) {
        return this.clanManager.getClan(key).orElse(null);
    }
}
