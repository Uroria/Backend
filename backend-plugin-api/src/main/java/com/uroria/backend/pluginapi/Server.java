package com.uroria.backend.pluginapi;

import com.uroria.backend.pluginapi.events.EventManager;
import com.uroria.backend.pluginapi.modules.*;
import com.uroria.backend.pluginapi.plugins.PluginManager;
import com.uroria.backend.pluginapi.scheduler.Scheduler;

public interface Server {

    PlayerManager getPlayerManager();
    StatsManager getStatsManager();
    PermissionManager getPermissionManager();
    PartyManager getPartyManager();
    ServerManager getServerManager();
    ClanManger getClanManager();
    FriendManager getFriendManager();
    SettingsManager getSettingsManager();

    PluginManager getPluginManager();
    EventManager getEventManager();
    Scheduler getScheduler();
}
