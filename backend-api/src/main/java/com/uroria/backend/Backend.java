package com.uroria.backend;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.UUID;

@UtilityClass
public class Backend {

    public Optional<User> getUser(UUID uuid) {
        return getWrapper().getUser(uuid);
    }

    public Optional<User> getUser(String username) {
        return getWrapper().getUser(username);
    }

    public Optional<Clan> getClan(String tag) {
        return getWrapper().getClan(tag);
    }

    public Optional<Server> getServer(long identifier) {
        return getWrapper().getServer(identifier);
    }

    public Optional<ServerGroup> getServerGroup(String name) {
        return getWrapper().getServerGroup(name);
    }

    public Optional<PermGroup> getPermissionGroup(String name) {
        return getWrapper().getPermissionGroup(name);
    }

    public BackendWrapper getWrapper() {
        return Unsafe.getInstance();
    }
}
