package com.uroria.backend;

import com.uroria.backend.clan.ClanManager;
import com.uroria.backend.friend.FriendManager;
import com.uroria.backend.permission.PermManager;
import com.uroria.backend.punishment.PunishmentManager;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.stats.StatsManager;
import com.uroria.backend.twitch.TwitchManager;
import com.uroria.backend.user.UserManager;

/**
 * The primary api-interface of the Backend.
 */
public interface Backend {

    PermManager getPermissionManager();

    TwitchManager getTwitchManager();

    UserManager getUserManager();

    FriendManager getFriendManager();

    ClanManager getClanManager();

    StatsManager getStatsManager();

    ServerManager getServerManager();

    PunishmentManager getPunishmentManager();

    /**
     * If you call this method, the whole network will shut down.
     * Just don't use it, if you don't know what you're doing!
     */
    void stopEverything();

    /**
     * Check if the Backend is online.
     */
    boolean isOnline();

    /**
     * Get the API used to interact with the backend.
     */
    static Backend getAPI() {
        return Unsafe.getInstance();
    }
}
