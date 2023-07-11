package com.uroria.backend.settings;

import com.uroria.backend.common.settings.BackendSettings;
import com.uroria.backend.common.pulsar.PulsarUpdate;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.util.function.Consumer;

public final class BackendSettingsUpdate extends PulsarUpdate<BackendSettings> {
    private final Consumer<BackendSettings> settingsConsumer;
    public BackendSettingsUpdate(PulsarClient pulsarClient, String bridgeName, Consumer<BackendSettings> settingsConsumer) throws PulsarClientException {
        super(pulsarClient, "settings:update", bridgeName);
        this.settingsConsumer = settingsConsumer;
    }

    @Override
    protected void onUpdate(BackendSettings object) {
        this.settingsConsumer.accept(object);
    }
}
