package com.uroria.backend.service.modules.clan;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.pulsar.PulsarResponse;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class ClanOperatorResponse extends PulsarResponse<Clan, UUID> {
    private final BackendClanManager clanManager;

    public ClanOperatorResponse(@NonNull PulsarClient pulsarClient, BackendClanManager clanManager) throws PulsarClientException {
        super(pulsarClient, "clan:request:operator", "clan:response:operator", clanManager.getModuleName());
        this.clanManager = clanManager;
    }

    @Override
    protected Clan response(@NonNull UUID key) {
        return this.clanManager.getClan(key, 0).orElse(null);
    }
}
