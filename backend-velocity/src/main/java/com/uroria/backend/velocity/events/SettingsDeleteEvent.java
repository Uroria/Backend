package com.uroria.backend.velocity.events;

import com.uroria.backend.settings.BackendSettings;

public final class SettingsDeleteEvent extends SettingsEvent {
    public SettingsDeleteEvent(BackendSettings settings) {
        super(settings);
    }
}
