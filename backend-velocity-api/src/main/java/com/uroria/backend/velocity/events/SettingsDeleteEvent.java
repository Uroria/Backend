package com.uroria.backend.velocity.events;

import com.uroria.backend.common.BackendSettings;

public final class SettingsDeleteEvent extends SettingsEvent {
    public SettingsDeleteEvent(BackendSettings settings) {
        super(settings);
    }
}
