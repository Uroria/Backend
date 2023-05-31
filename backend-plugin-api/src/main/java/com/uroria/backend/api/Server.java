package com.uroria.backend.api;

import com.uroria.backend.api.events.EventManager;
import com.uroria.backend.api.modules.PartyManager;
import com.uroria.backend.api.modules.PermissionManager;
import com.uroria.backend.api.modules.PlayerManager;
import com.uroria.backend.api.modules.StatsManager;
import com.uroria.backend.api.plugins.PluginManager;
import com.uroria.backend.api.scheduler.Scheduler;

public interface Server {

    PlayerManager getPlayerManager();
    StatsManager getStatsManager();
    PermissionManager getPermissionManager();
    PartyManager getPartyManager();

    PluginManager getPluginManager();
    EventManager getEventManager();
    Scheduler getScheduler();
}
