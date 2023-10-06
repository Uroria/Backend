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
import lombok.experimental.UtilityClass;

import java.util.Collection;
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

    public Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator) {
        return getWrapper().createClan(name, tag, operator);
    }

    public Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator, long foundingDate) {
        return getWrapper().createClan(name, tag, operator, foundingDate);
    }

    public Result<Server> getServer(long identifier) {
        return getWrapper().getServer(identifier);
    }
    @SuppressWarnings("WarningMarkers")
    @Warning(message = "Ordering a server could take more than 30 seconds. Use this method only if you know what you're doing.", suppress = "Okay, I understand")
    public Result<Server> createServer(int templateId, ServerGroup group) {
        return getWrapper().createServer(templateId, group);
    }

    public Collection<Server> getServers() {
        return getWrapper().getServers();
    }

    public Result<Proxy> getProxy(long identifier) {
        return getWrapper().getProxy(identifier);
    }

    public Collection<Proxy> getProxies(String name) {
        return getWrapper().getProxies(name);
    }

    public Collection<Proxy> getProxies() {
        return getWrapper().getProxies();
    }

    public Result<ServerGroup> getServerGroup(String name) {
        return getWrapper().getServerGroup(name);
    }

    public Result<ServerGroup> createGroup(String name, int maxPlayers) {
        return getWrapper().createServerGroup(name, maxPlayers);
    }

    public Collection<ServerGroup> getServerGroups() {
        return getWrapper().getServerGroups();
    }

    public Result<PermGroup> getPermissionGroup(String name) {
        return getWrapper().getPermissionGroup(name);
    }

    public Result<PermGroup> createPermissionGroup(String name) {
        return getWrapper().createPermissionGroup(name);
    }

    public Collection<PermGroup> getPermissionGroups() {
        return getWrapper().getPermissionGroups();
    }

    public EventManager getEventManager() {
        return getWrapper().getEventManager();
    }

    public BackendWrapper getWrapper() {
        //noinspection WeakWarningMarkers
        return Unsafe.getInstance();
    }
}
