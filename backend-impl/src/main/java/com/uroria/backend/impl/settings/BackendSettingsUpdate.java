package com.uroria.backend.impl.settings;

import com.uroria.backend.pulsar.PulsarUpdate;
import com.uroria.backend.settings.BackendSettings;
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
