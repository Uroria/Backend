package com.uroria.backend.server.modules.settings;

import com.uroria.backend.settings.BackendSettings;
import com.uroria.backend.settings.SettingsRequest;
import com.uroria.backend.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.UUID;

public final class BackendSettingsIDResponse extends PulsarResponse<SettingsRequest, BackendSettings> {
    private final BackendSettingsManager settingsManager;
    public BackendSettingsIDResponse(PulsarClient pulsarClient, BackendSettingsManager settingsManager) throws PulsarClientException {
        super(pulsarClient, "settings:request:3", "settings:response:3", "SettingsModule");
        this.settingsManager = settingsManager;
    }

    @Override
    protected BackendSettings response(SettingsRequest key) {
        UUID uuid = key.getUUID().orElse(null);
        Integer gameID = key.getGameID().orElse(null);
        Integer id = key.getID().orElse(null);
        if (uuid == null || gameID == null || id == null) return null;
        return settingsManager.getSettings(uuid, gameID, id).orElse(null);
    }
}
