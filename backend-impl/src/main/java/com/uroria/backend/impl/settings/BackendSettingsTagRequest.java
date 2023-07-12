package com.uroria.backend.impl.settings;

import com.uroria.backend.pulsar.PulsarRequest;
import com.uroria.backend.settings.BackendSettings;
import com.uroria.backend.settings.SettingsRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class BackendSettingsTagRequest extends PulsarRequest<BackendSettings, SettingsRequest> {
    public BackendSettingsTagRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "settings:request:1", "settings:response:1", bridgeName, 4000, 20);
    }
}