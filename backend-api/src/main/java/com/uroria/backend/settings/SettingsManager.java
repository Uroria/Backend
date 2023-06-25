package com.uroria.backend.settings;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.BackendSettings;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class SettingsManager extends AbstractManager {
    protected final Logger logger;
    protected final Collection<BackendSettings> settings;

    public SettingsManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.settings = new CopyOnWriteArrayList<>();
    }

    abstract protected void checkSettings(BackendSettings settings);

    abstract public Collection<BackendSettings> getSettings(UUID uuid, int gameId);

    abstract public Optional<BackendSettings> getSettings(UUID uuid, int gameId, int id);

    abstract public Optional<BackendSettings> getSettings(String tag);

    abstract public void updateSettings(BackendSettings settings);
}
