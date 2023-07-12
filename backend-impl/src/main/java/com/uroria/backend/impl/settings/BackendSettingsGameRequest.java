package com.uroria.backend.impl.settings;

import com.uroria.backend.pulsar.PulsarRequest;
import com.uroria.backend.settings.BackendSettings;
import com.uroria.backend.settings.SettingsRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.Collection;

public final class BackendSettingsGameRequest extends PulsarRequest<Collection<BackendSettings>, SettingsRequest> {
    public BackendSettingsGameRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "settings:request:2", "settings:response:2", bridgeName, 4000, 20);
    }
}
