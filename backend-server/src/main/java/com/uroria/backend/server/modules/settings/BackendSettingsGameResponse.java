package com.uroria.backend.server.modules.settings;

import com.uroria.backend.settings.BackendSettings;
import com.uroria.backend.settings.SettingsRequest;
import com.uroria.backend.pulsar.PulsarResponse;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public final class BackendSettingsGameResponse extends PulsarResponse<SettingsRequest, Collection<BackendSettings>> {
    private final BackendSettingsManager settingsManager;

    public BackendSettingsGameResponse(PulsarClient pulsarClient, BackendSettingsManager settingsManager) throws PulsarClientException {
        super(pulsarClient, "settings:request:2", "settings:response:2", "SettingsModule");
        this.settingsManager = settingsManager;
    }

    @Override
    protected Collection<BackendSettings> response(SettingsRequest key) {
        UUID uuid = key.getUUID().orElse(null);
        Integer gameId = key.getGameID().orElse(null);
        if (uuid == null || gameId == null) return new ArrayList<>();
        return this.settingsManager.getSettings(uuid, gameId);
    }
}
