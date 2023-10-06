package com.uroria.backend;

import com.uroria.annotations.markers.Warning;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import com.uroria.base.event.EventManager;
import com.uroria.problemo.result.Result;
import lombok.NonNull;

import java.util.Collection;
import java.util.UUID;

public interface BackendWrapper {

    Result<User> getUser(UUID uuid);

    Result<User> getUser(String username);

    Result<Clan> getClan(String tag);

    default Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator) {
        return createClan(name, tag, operator, System.currentTimeMillis());
    }

    Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator, long foundingDate);

    Result<Server> getServer(long identifier);

    Collection<Server> getServers();

    @SuppressWarnings("WarningMarkers")
    @Warning(message = "Ordering a server could take more than 30 seconds. Use this method only if you know what you're doing.", suppress = "Okay, I understand")
    default Result<Server> createServer(int templateId, @NonNull ServerGroup group) {
        return group.createServer(templateId);
    }

    Result<Proxy> getProxy(long identifier);

    Result<Proxy> createProxy(String name, int maxPlayers);

    Collection<Proxy> getProxies(String name);

    Collection<Proxy> getProxies();

    Result<ServerGroup> getServerGroup(String type);

    Collection<ServerGroup> getServerGroups();

    Result<ServerGroup> createServerGroup(String name, int maxPlayers);

    Result<PermGroup> getPermissionGroup(String name);

    Collection<PermGroup> getPermissionGroups();

    Result<PermGroup> createPermissionGroup(String name);

    EventManager getEventManager();
}
