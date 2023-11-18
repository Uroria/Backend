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
import com.uroria.base.scheduler.Scheduler;
import com.uroria.problemo.result.Result;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;

import java.util.Collection;
import java.util.UUID;

import static com.uroria.backend.Unsafe.getInstance;

public interface Backend {

    /**
     * Searches for an existing database entry for the given uuid of a user
     * and if not exists, create a new one.
     */
    Result<User> getUser(UUID uuid);
    
    static Result<User> user(UUID uuid) {
        return getInstance().getUser(uuid);
    }

    /**
     * Looks for a User with the given username.
     */
    Result<User> getUser(String username);
    
    static Result<User> user(String username) {
        return getInstance().getUser(username);
    }

    @ApiStatus.Experimental
    Result<User> getUser(long discordUserId);
    
    static Result<User> user(long discordUserId) {
        return getInstance().getUser(discordUserId);
    }

    /**
     * Gets a clan by it's changeable tag.
     */
    Result<Clan> getClan(String tag);
    
    static Result<Clan> clan(String tag) {
        return getInstance().getClan(tag);
    }

    default Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator) {
        return createClan(name, tag, operator, System.currentTimeMillis());
    }

    Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator, long foundingDate);

    /**
     * Gets a registered server with the given id.
     */
    Result<Server> getServer(long identifier);
    
    static Result<Server> server(long identifier) {
        return getInstance().getServer(identifier);
    }

    /**
     * Gets all existing servers.
     */
    @TimeConsuming
    Collection<Server> getServers();
    
    static Collection<Server> servers() {
        return getInstance().getServers();
    }

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
    
    static Result<Proxy> proxy(long identifier) {
        return getInstance().getProxy(identifier);
    }

    /**
     * Creates a proxy instance.
     */
    @ApiStatus.Experimental
    Result<Proxy> createProxy(String name, int templateId, int maxPlayers);

    Collection<Proxy> getProxies(String name);
    
    static Collection<Proxy> proxies(String name) {
        return getInstance().getProxies(name);
    }

    Collection<Proxy> getProxies();
    
    static Collection<Proxy> proxies() {
        return getInstance().getProxies();
    }

    Result<ServerGroup> getServerGroup(String type);
    
    static Result<ServerGroup> serverGroup(String type) {
        return getInstance().getServerGroup(type);
    }

    Collection<ServerGroup> getServerGroups();
    
    static Collection<ServerGroup> serverGroups() {
        return getInstance().getServerGroups();
    }

    Result<ServerGroup> createServerGroup(String name, int maxPlayers);

    Result<PermGroup> getPermissionGroup(String name);
    
    static Result<PermGroup> permissionGroup(String name) {
        return getInstance().getPermissionGroup(name);
    }

    Collection<PermGroup> getPermissionGroups();
    
    static Collection<PermGroup> permissionGroups() {
        return getInstance().getPermissionGroups();
    }

    Result<PermGroup> createPermissionGroup(String name);

    Statistics getStatistics();
    
    static Statistics statistics() {
        return getInstance().getStatistics();
    }

    Scheduler getScheduler();
    
    static Scheduler scheduler() {
        return getInstance().getScheduler();
    }

    EventManager getEventManager();
    
    static EventManager eventManager() {
        return getInstance().getEventManager();
    }

    WrapperEnvironment getEnvironment();
    
    static WrapperEnvironment environment() {
        return getInstance().getEnvironment();
    }

    static Backend wrapper() {
        return getInstance();
    }
}
