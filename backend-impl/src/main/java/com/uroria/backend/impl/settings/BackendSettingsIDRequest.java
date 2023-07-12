package com.uroria.backend.impl.settings;

import com.uroria.backend.pulsar.PulsarRequest;
import com.uroria.backend.settings.BackendSettings;
import com.uroria.backend.settings.SettingsRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class BackendSettingsIDRequest extends PulsarRequest<BackendSettings, SettingsRequest> {
    public BackendSettingsIDRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "settings:request:3", "settings:response:3", bridgeName, 4000, 20);
    }
}
