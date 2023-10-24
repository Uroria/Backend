package com.uroria.backend;

import com.uroria.annotations.markers.Warning;
import com.uroria.annotations.safety.TimeConsuming;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.stats.Statistics;
import com.uroria.backend.user.User;
import com.uroria.base.event.EventManager;
import com.uroria.problemo.result.Result;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.UUID;

public interface BackendWrapper {

    /**
     * Searches for an existing database entry for the given uuid of a user
     * and if not exists, create a new one.
     */
    Result<User> getUser(UUID uuid);

    /**
     * Looks for a User with the given username.
     */
    Result<User> getUser(String username);

    Result<User> getUser(long discordUserId);

    /**
     * Gets a clan by it's changeable tag.
     */
    Result<Clan> getClan(String tag);

    default Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator) {
        return createClan(name, tag, operator, System.currentTimeMillis());
    }

    Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator, long foundingDate);

    /**
     * Gets a registered server with the given id.
     */
    Result<Server> getServer(long identifier);

    /**
     * Gets all existing servers.
     */
    @TimeConsuming
    Collection<Server> getServers();

    /**
     * Creates a server instance.
     * You need a {@link ServerGroup} for sorting purposes.
     */
    @SuppressWarnings("WarningMarkers")
    @Warning(message = "Ordering a server could take more than 30 seconds. Use this method only if you know what you're doing.", suppress = "Okay, I understand")
    default Result<Server> createServer(int templateId, @NonNull ServerGroup group) {
        return group.createServer(templateId);
    }

    /**
     * Gets a registered proxy with the given id.
     */
    Result<Proxy> getProxy(long identifier);

    /**
     * Creates a proxy instance.
     */
    @ApiStatus.Experimental
    Result<Proxy> createProxy(String name, int templateId, int maxPlayers);

    Collection<Proxy> getProxies(String name);

    Collection<Proxy> getProxies();

    Result<ServerGroup> getServerGroup(String type);

    Collection<ServerGroup> getServerGroups();

    Result<ServerGroup> createServerGroup(String name, int maxPlayers);

    Result<PermGroup> getPermissionGroup(String name);

    Collection<PermGroup> getPermissionGroups();

    Result<PermGroup> createPermissionGroup(String name);

    Statistics getStatistics();

    EventManager getEventManager();
}
