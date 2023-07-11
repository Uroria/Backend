package com.uroria.backend.settings;

import com.uroria.backend.AbstractManager;
import com.uroria.backend.common.settings.BackendSettings;
import com.uroria.backend.common.settings.SettingsManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractSettingsManager extends AbstractManager implements SettingsManager {
    protected final Logger logger;
    protected final Collection<BackendSettings> settings;

    public AbstractSettingsManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient);
        this.logger = logger;
        this.settings = new CopyOnWriteArrayList<>();
    }

    abstract protected void checkSettings(BackendSettings settings);
}
