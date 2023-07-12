package com.uroria.backend.pluginapi;

import com.uroria.backend.clan.ClanManager;
import com.uroria.backend.friends.FriendManager;
import com.uroria.backend.party.PartyManager;
import com.uroria.backend.permission.PermissionManager;
import com.uroria.backend.player.PlayerManager;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.settings.SettingsManager;
import com.uroria.backend.stats.StatsManager;
import com.uroria.backend.pluginapi.events.EventManager;
import com.uroria.backend.pluginapi.plugins.PluginManager;
import com.uroria.backend.pluginapi.scheduler.Scheduler;

public interface Server {

    PlayerManager getPlayerManager();
    StatsManager getStatsManager();
    PermissionManager getPermissionManager();
    PartyManager getPartyManager();
    ServerManager getServerManager();
    ClanManager getClanManager();
    FriendManager getFriendManager();
    SettingsManager getSettingsManager();

    PluginManager getPluginManager();
    EventManager getEventManager();
    Scheduler getScheduler();
}
