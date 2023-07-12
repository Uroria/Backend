package com.uroria.backend.velocity.events;

import com.uroria.backend.settings.BackendSettings;

public final class SettingsUpdateEvent extends SettingsEvent {
    public SettingsUpdateEvent(BackendSettings settings) {
        super(settings);
    }
}
