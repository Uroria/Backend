package com.uroria.backend.service.modules.clan;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class ClanTagResponse extends PulsarResponse<ClanOld, String> {
    private final BackendClanManager clanManager;

    public ClanTagResponse(@NonNull PulsarClient pulsarClient, BackendClanManager clanManager) throws PulsarClientException {
        super(pulsarClient, "clan:request:tag", "clan:response:tag", clanManager.getModuleName());
        this.clanManager = clanManager;
    }

    @Override
    protected ClanOld response(@NonNull String key) {
        return this.clanManager.getClan(key, 0).orElse(null);
    }
}
