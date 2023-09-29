package com.uroria.backend;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import com.uroria.base.event.EventManager;
import com.uroria.problemo.result.Result;

import java.util.UUID;

public interface BackendWrapper {

    Result<User> getUser(UUID uuid);

    Result<User> getUser(String username);

    Result<Clan> getClan(String tag);

    Result<Server> getServer(long identifier);

    Result<ServerGroup> getServerGroup(String name);

    Result<PermGroup> getPermissionGroup(String name);

    EventManager getEventManager();
}
