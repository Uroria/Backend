package com.uroria.backend.server.modules.settings;

import com.uroria.backend.common.settings.BackendSettings;
import com.uroria.backend.common.settings.SettingsRequest;
import com.uroria.backend.common.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public class BackendSettingsTagResponse extends PulsarResponse<SettingsRequest, BackendSettings> {
    private final BackendSettingsManager settingsManager;
    public BackendSettingsTagResponse(PulsarClient pulsarClient, BackendSettingsManager settingsManager) throws PulsarClientException {
        super(pulsarClient, "settings:request:1", "settings:response:1", "SettingsModule");
        this.settingsManager = settingsManager;
    }

    @Override
    protected BackendSettings response(SettingsRequest key) {
        if (key.getTag().isEmpty()) return null;
        return settingsManager.getSettings(key.getTag().get()).orElse(null);
    }
}
