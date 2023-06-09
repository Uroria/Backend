package com.uroria.backend.api;

import com.uroria.backend.api.events.EventManager;
import com.uroria.backend.api.modules.*;
import com.uroria.backend.api.plugins.PluginManager;
import com.uroria.backend.api.scheduler.Scheduler;

public interface Server {

    PlayerManager getPlayerManager();
    StatsManager getStatsManager();
    PermissionManager getPermissionManager();
    PartyManager getPartyManager();
    ServerManager getServerManager();

    PluginManager getPluginManager();
    EventManager getEventManager();
    Scheduler getScheduler();
}
