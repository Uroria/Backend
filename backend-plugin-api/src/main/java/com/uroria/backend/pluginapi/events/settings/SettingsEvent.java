package com.uroria.backend.pluginapi.events.settings;

import com.uroria.backend.common.settings.BackendSettings;
import com.uroria.backend.pluginapi.events.Event;

public abstract class SettingsEvent extends Event {
    private final BackendSettings settings;

    protected SettingsEvent(BackendSettings settings) {
        this.settings = settings;
    }

    public BackendSettings getSettings() {
        return settings;
    }
}
