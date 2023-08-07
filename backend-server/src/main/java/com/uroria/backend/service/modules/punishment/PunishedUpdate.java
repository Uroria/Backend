package com.uroria.backend.service.modules.punishment;

import com.uroria.backend.impl.pulsar.PulsarUpdate;
import com.uroria.backend.punishment.Punished;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class PunishedUpdate extends PulsarUpdate<Punished> {
    private final BackendPunishmentManager punishmentManager;

    public PunishedUpdate(@NonNull PulsarClient pulsarClient, BackendPunishmentManager punishmentManager) throws PulsarClientException {
        super(pulsarClient, "punished:update", "PunishedModule");
        this.punishmentManager = punishmentManager;
    }

    @Override
    protected void onUpdate(Punished object) {
        this.punishmentManager.updateDatabase(object);
    }
}
