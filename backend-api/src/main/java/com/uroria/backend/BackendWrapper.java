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

import java.util.Collection;
import java.util.UUID;

public interface BackendWrapper {

    Result<User> getUser(UUID uuid);

    Result<User> getUser(String username);

    Result<Clan> getClan(String tag);

    Result<Server> getServer(long identifier);

    @Warning(message = "Ordering a server could take more than 30 seconds. Use this method only if you know what you're doing.", suppress = "Okay, I understand")
    default Result<Server> createServer(int templateId, ServerGroup group) {
        return group.createServer(templateId);
    }

    Result<Proxy> getProxy(long identifier);

    Collection<Proxy> getProxies(String name);

    Result<ServerGroup> getServerGroup(String type);

    Result<PermGroup> getPermissionGroup(String name);

    Result<PermGroup> createPermissionGroup(String name);

    EventManager getEventManager();
}
