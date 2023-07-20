package com.uroria.backend.impl.settings;

import com.uroria.backend.impl.AbstractManager;
import com.uroria.backend.settings.BackendSettings;
import com.uroria.backend.settings.SettingsManager;
import com.uroria.backend.utils.ObjectUtils;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Set;

public abstract class AbstractSettingsManager extends AbstractManager implements SettingsManager {
    protected final Set<BackendSettings> settings;

    public AbstractSettingsManager(PulsarClient pulsarClient, Logger logger) {
        super(pulsarClient, logger);
        this.settings = ObjectUtils.newSet();
    }

    abstract protected void checkSettings(BackendSettings settings);
}
