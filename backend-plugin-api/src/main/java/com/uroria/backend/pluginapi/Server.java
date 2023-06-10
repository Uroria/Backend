package com.uroria.backend.pluginapi;

import com.uroria.backend.pluginapi.events.EventManager;
import com.uroria.backend.pluginapi.modules.ClanManger;
import com.uroria.backend.pluginapi.modules.FriendManager;
import com.uroria.backend.pluginapi.modules.PartyManager;
import com.uroria.backend.pluginapi.modules.PermissionManager;
import com.uroria.backend.pluginapi.modules.PlayerManager;
import com.uroria.backend.pluginapi.modules.ServerManager;
import com.uroria.backend.pluginapi.modules.StatsManager;
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

    PluginManager getPluginManager();
    EventManager getEventManager();
    Scheduler getScheduler();
}
