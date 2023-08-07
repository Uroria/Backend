package com.uroria.backend.service.modules.punishment;

import com.uroria.backend.impl.pulsar.PulsarResponse;
import com.uroria.backend.punishment.Punished;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class PunishedResponse extends PulsarResponse<Punished, UUID> {
    private final BackendPunishmentManager punishmentManager;

    public PunishedResponse(@NonNull PulsarClient pulsarClient, BackendPunishmentManager punishmentManager) throws PulsarClientException {
        super(pulsarClient, "punished:request", "punished:response", "PunishedModule");
        this.punishmentManager = punishmentManager;
    }

    @Override
    protected Punished response(@NonNull UUID key) {
        return this.punishmentManager.getPunished(key).orElse(null);
    }
}
