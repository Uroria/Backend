package com.uroria.backend.common;

import com.uroria.backend.common.permission.PermissionManager;
import com.uroria.backend.common.player.PlayerManager;
import com.uroria.backend.common.server.ServerManager;
import com.uroria.backend.common.settings.SettingsManager;
import com.uroria.backend.common.stats.StatsManager;

public interface BackendAPI {

    PlayerManager getPlayerManager();
    PermissionManager getPermissionManager();
    ServerManager getServerManager();
    StatsManager getStatsManager();
    SettingsManager getSettingsManager();

    static BackendAPI getAPI() {
        return Unsafe.getAPI();
    }
}
