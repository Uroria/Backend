package com.uroria.backend.server.modules.settings;

import com.uroria.backend.settings.BackendSettings;
import com.uroria.backend.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

public final class BackendSettingsUpdate extends PulsarUpdate<BackendSettings> {
    private final BackendSettingsManager settingsManager;
    public BackendSettingsUpdate(PulsarClient pulsarClient, BackendSettingsManager settingsManager) throws PulsarClientException {
        super(pulsarClient, "settings:update", "SettingsModule");
        this.settingsManager = settingsManager;
    }

    @Override
    protected void onUpdate(BackendSettings object) {
        this.settingsManager.updateLocal(object);
    }
}
