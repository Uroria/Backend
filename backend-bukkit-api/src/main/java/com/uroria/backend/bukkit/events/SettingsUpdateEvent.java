package com.uroria.backend.bukkit.events;

import com.uroria.backend.common.settings.BackendSettings;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public final class SettingsUpdateEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final BackendSettings settings;

    public SettingsUpdateEvent(BackendSettings settings) {
        this.settings = settings;
    }

    public BackendSettings getSettings() {
        return settings;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
