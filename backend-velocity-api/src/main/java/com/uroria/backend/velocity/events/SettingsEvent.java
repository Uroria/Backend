package com.uroria.backend.velocity.events;

import com.uroria.backend.common.settings.BackendSettings;

public abstract class SettingsEvent {
    private final BackendSettings settings;

    public SettingsEvent(BackendSettings settings) {
        this.settings = settings;
    }

    public BackendSettings getSettings() {
        return settings;
    }
}
