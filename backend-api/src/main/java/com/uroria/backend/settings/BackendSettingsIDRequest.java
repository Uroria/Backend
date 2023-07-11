package com.uroria.backend.settings;

import com.uroria.backend.common.settings.BackendSettings;
import com.uroria.backend.common.settings.SettingsRequest;
import com.uroria.backend.common.pulsar.PulsarRequest;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class BackendSettingsIDRequest extends PulsarRequest<BackendSettings, SettingsRequest> {
    public BackendSettingsIDRequest(PulsarClient pulsarClient, String bridgeName) throws PulsarClientException {
        super(pulsarClient, "settings:request:3", "settings:response:3", bridgeName, 4000, 20);
    }
}
