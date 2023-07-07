package com.uroria.backend.pluginapi.events.settings;

import com.uroria.backend.common.BackendSettings;

public class SettingsDeleteEvent extends SettingsEvent {
    public SettingsDeleteEvent(BackendSettings settings) {
        super(settings);
    }
}
