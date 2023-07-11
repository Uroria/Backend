package com.uroria.backend.pluginapi.events.settings;

import com.uroria.backend.common.settings.BackendSettings;

public final class SettingsRegisterEvent extends SettingsEvent {
    public SettingsRegisterEvent(BackendSettings settings) {
        super(settings);
    }
}
