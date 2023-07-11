package com.uroria.backend.pluginapi;

import com.uroria.backend.common.clan.ClanManager;
import com.uroria.backend.common.friends.FriendManager;
import com.uroria.backend.common.party.PartyManager;
import com.uroria.backend.common.permission.PermissionManager;
import com.uroria.backend.common.player.PlayerManager;
import com.uroria.backend.common.server.ServerManager;
import com.uroria.backend.common.settings.SettingsManager;
import com.uroria.backend.common.stats.StatsManager;
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
