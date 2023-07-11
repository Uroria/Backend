package com.uroria.backend.pluginapi.events.settings;

import com.uroria.backend.common.settings.BackendSettings;

public final class SettingsUpdateEvent extends SettingsEvent {

    public SettingsUpdateEvent(BackendSettings settings) {
        super(settings);
    }
}
