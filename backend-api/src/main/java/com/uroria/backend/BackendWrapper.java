package com.uroria.backend;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;

import java.util.Optional;
import java.util.UUID;

public interface BackendWrapper {

    Optional<User> getUser(UUID uuid);

    Optional<User> getUser(String username);

    Optional<Clan> getClan(String tag);

    Optional<Server> getServer(long identifier);

    Optional<ServerGroup> getServerGroup(String name);

    Optional<PermGroup> getPermissionGroup(String name);
}
