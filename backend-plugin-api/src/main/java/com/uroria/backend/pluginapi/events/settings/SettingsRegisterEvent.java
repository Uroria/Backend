package com.uroria.backend.pluginapi.events.settings;

import com.uroria.backend.settings.BackendSettings;

public final class SettingsRegisterEvent extends SettingsEvent {
    public SettingsRegisterEvent(BackendSettings settings) {
        super(settings);
    }
}
