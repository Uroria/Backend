package com.uroria.backend;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import com.uroria.base.event.EventManager;
import com.uroria.problemo.result.Result;
import lombok.experimental.UtilityClass;

import java.util.UUID;

@UtilityClass
public class Backend {

    public Result<User> getUser(UUID uuid) {
        return getWrapper().getUser(uuid);
    }

    public Result<User> getUser(String username) {
        return getWrapper().getUser(username);
    }

    public Result<Clan> getClan(String tag) {
        return getWrapper().getClan(tag);
    }

    public Result<Server> getServer(long identifier) {
        return getWrapper().getServer(identifier);
    }

    public Result<ServerGroup> getServerGroup(String name) {
        return getWrapper().getServerGroup(name);
    }

    public Result<PermGroup> getPermissionGroup(String name) {
        return getWrapper().getPermissionGroup(name);
    }

    public EventManager getEventManager() {
        return getWrapper().getEventManager();
    }

    public BackendWrapper getWrapper() {
        return Unsafe.getInstance();
    }
}
