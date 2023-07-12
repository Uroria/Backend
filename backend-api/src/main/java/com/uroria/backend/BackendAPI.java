package com.uroria.backend;

import com.uroria.backend.management.RootManager;
import com.uroria.backend.messenger.MessageManager;
import com.uroria.backend.permission.PermissionManager;
import com.uroria.backend.player.PlayerManager;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.settings.SettingsManager;
import com.uroria.backend.stats.StatsManager;

public interface BackendAPI {

    PlayerManager getPlayerManager();
    PermissionManager getPermissionManager();
    ServerManager getServerManager();
    StatsManager getStatsManager();
    SettingsManager getSettingsManager();
    MessageManager getMessageManager();
    RootManager getRootManager();

    static BackendAPI getAPI() {
        return Unsafe.getAPI();
    }
}
