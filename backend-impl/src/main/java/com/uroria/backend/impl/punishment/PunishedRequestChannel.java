package com.uroria.backend.impl.punishment;

import com.uroria.backend.impl.pulsar.PulsarRequest;
import com.uroria.backend.punishment.Punished;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class PunishedRequestChannel extends PulsarRequest<Punished, UUID> {
    public PunishedRequestChannel(@NonNull PulsarClient pulsarClient, @NonNull String name) throws PulsarClientException {
        super(pulsarClient, "punished:request", "punished:response", name, 5000);
    }
}
