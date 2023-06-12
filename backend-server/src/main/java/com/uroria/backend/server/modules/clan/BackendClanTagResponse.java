package com.uroria.backend.server.modules.clan;

import com.uroria.backend.common.BackendClan;
import com.uroria.backend.common.pulsar.PulsarResponse;
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
        LOGGER.debug("Requesting clan by tag " + key);
        return this.clanManager.getClan(key).orElse(null);
    }
}
