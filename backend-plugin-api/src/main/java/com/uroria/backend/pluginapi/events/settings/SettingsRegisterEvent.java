package com.uroria.backend.pluginapi.events.settings;

import com.uroria.backend.common.BackendSettings;

public final class SettingsRegisterEvent extends SettingsEvent {
    public SettingsRegisterEvent(BackendSettings settings) {
        super(settings);
    }
}
