package com.uroria.backend.impl;

import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.clan.ClanManager;
import com.uroria.backend.impl.clan.ClanWrapper;
import com.uroria.backend.impl.permission.GroupWrapper;
import com.uroria.backend.impl.permission.PermGroupManager;
import com.uroria.backend.impl.proxy.ProxyManager;
import com.uroria.backend.impl.proxy.ProxyWrapper;
import com.uroria.backend.impl.server.ServerManager;
import com.uroria.backend.impl.server.ServerWrapper;
import com.uroria.backend.impl.server.group.ServerGroupManager;
import com.uroria.backend.impl.stats.StatsManager;
import com.uroria.backend.impl.user.UserManager;
import com.uroria.backend.impl.user.UserWrapper;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.stats.Statistics;
import com.uroria.backend.user.User;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.NonNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.UUID;

public class BackendWrapperImpl extends AbstractBackendWrapper {

    private final UserManager userManager;
    private final ProxyManager proxyManager;
    private final PermGroupManager permGroupManager;
    private final ServerGroupManager serverGroupManager;
    private final ServerManager serverManager;
    private final ClanManager clanManager;
    private final StatsManager statsManager;

    public BackendWrapperImpl(@NonNull Logger logger) {
        super(logger);
        this.statsManager = new StatsManager(communicator);
        this.userManager = new UserManager(this.statsManager, communicator);
        this.proxyManager = new ProxyManager(communicator);
        this.permGroupManager = new PermGroupManager(communicator);
        this.serverManager = new ServerManager(communicator);
        this.serverGroupManager = new ServerGroupManager(serverManager, communicator);
        this.clanManager = new ClanManager(communicator);
    }

    @Override
    public void start() throws Exception {
        this.userManager.enable();
        this.proxyManager.enable();
        this.permGroupManager.enable();
        this.serverGroupManager.enable();
        this.serverManager.enable();
        this.clanManager.enable();
    }

    @Override
    public void shutdown() throws Exception {
        this.userManager.disable();
        this.proxyManager.disable();
        this.permGroupManager.disable();
        this.serverGroupManager.disable();
        this.serverManager.disable();
        this.clanManager.disable();
        super.shutdown();
    }

    @Override
    public Result<User> getUser(UUID uuid) {
        try {
            if (uuid == null) return Result.none();
            UserWrapper wrapper = this.userManager.getUserWrapper(uuid);
            if (wrapper == null) return Result.none();
            return Result.some(wrapper);
        } catch (Exception exception) {
            this.logger.error("Unable to get user " + uuid, exception);
            return Result.problem(Problem.error("Unable to request user", exception));
        }
    }

    @Override
    public Result<User> getUser(String username) {
        try {
            if (username == null) return Result.none();
            UserWrapper wrapper = this.userManager.getUserWrapper(username);
            if (wrapper == null) return Result.none();
            return Result.some(wrapper);
        } catch (Exception exception) {
            this.logger.error("Unable to get user by name " + username, exception);
            return Result.problem(Problem.error("Unable to request user", exception));
        }
    }

    @Override
    public Result<User> getUser(long discordUserId) {
        return Result.none();
    }

    @Override
    public Result<Clan> getClan(String tag) {
        try {
            if (tag == null) return Result.none();
            ClanWrapper wrapper = this.clanManager.getClanWrapper(tag);
            if (wrapper == null) return Result.none();
            return Result.some(wrapper);
        } catch (Exception exception) {
            this.logger.error("Unable to get clan with tag " + tag, exception);
            return Result.problem(Problem.error("Unable to request clan", exception));
        }
    }

    @Override
    public Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator, long foundingDate) {
        try {
            ClanWrapper wrapper = this.clanManager.createClanWrapper(name, tag, operator, foundingDate);
            if (wrapper == null) return Result.none();
            return Result.some(wrapper);
        } catch (Exception exception) {
            this.logger.error("Unable to create clan with name " + name + ", tag " + tag + " and operator " + operator, exception);
            return Result.problem(Problem.error("Unable to create clan", exception));
        }
    }

    @Override
    public Result<Server> getServer(long identifier) {
        try {
            if (identifier == 0) return Result.none();
            ServerWrapper wrapper = this.serverManager.getServerWrapper(identifier);
            if (wrapper == null) return Result.none();
            return Result.some(wrapper);
        } catch (Exception exception) {
            this.logger.error("Unable to request server " + identifier, exception);
            return Result.problem(Problem.error("Unable to get server", exception));
        }
    }

    @Override
    public Collection<Server> getServers() {
        try {
            return this.serverManager.getAll();
        } catch (Exception exception) {
            this.logger.error("Unable to request all servers", exception);
            return ObjectSets.emptySet();
        }
    }

    @Override
    public Result<Proxy> getProxy(long identifier) {
        try {
            if (identifier == 0) return Result.none();
            ProxyWrapper wrapper = this.proxyManager.getProxyWrapper(identifier);
            if (wrapper == null) return Result.none();
            return Result.some(wrapper);
        } catch (Exception exception) {
            this.logger.error("Unable to request proxy " + identifier, exception);
            return Result.problem(Problem.error("Unable to request proxy", exception));
        }
    }

    @Override
    public Result<Proxy> createProxy(String name, int templateId, int maxPlayers) {
        try {
            if (name == null) return Result.none();
            if (templateId == 0) return Result.none();
            if (maxPlayers == 0) return Result.none();
            ProxyWrapper wrapper = this.proxyManager.createProxyWrapper(name, templateId, maxPlayers);
            if (wrapper == null) return Result.none();
            return Result.some(wrapper);
        } catch (Exception exception) {
            this.logger.error("Unable to create proxy with name" + name + " and TID " + templateId, exception);
            return Result.problem(Problem.error("Unable to create proxy", exception));
        }
    }

    @Override
    public Collection<Proxy> getProxies(String name) {
        try {
            if (name == null) return ObjectSets.emptySet();

        }
    }

    @Override
    public Collection<Proxy> getProxies() {
        return null;
    }

    @Override
    public Result<ServerGroup> getServerGroup(String type) {
        try {

        }
    }

    @Override
    public Collection<ServerGroup> getServerGroups() {
        return null;
    }

    @Override
    public Result<ServerGroup> createServerGroup(String name, int maxPlayers) {
        return null;
    }

    @Override
    public Result<PermGroup> getPermissionGroup(String name) {
        try {
            if (name == null) return Result.none();
            GroupWrapper wrapper = this.permGroupManager.getGroupWrapper(name);
            if (wrapper == null) return Result.none();
            return Result.some(wrapper);
        } catch (Exception exception) {
            this.logger.error("Unable to get perm-group " + name, exception);
            return Result.problem(Problem.error("Unable to get perm-group", exception));
        }
    }

    @Override
    public Collection<PermGroup> getPermissionGroups() {
        try {

        } catch (Exception exception) {

        }
    }

    @Override
    public Result<PermGroup> createPermissionGroup(String name) {
        try {
            if (name == null) return Result.none();
            GroupWrapper wrapper = this.permGroupManager.createGroupWrapper(name, 999);
            if (wrapper == null) return Result.none();
            return Result.some(wrapper);
        } catch (Exception exception) {
            this.logger.error("Unable to get perm-group " + name, exception);
            return Result.problem(Problem.error("Unable to get perm-group", exception));
        }
    }

    @Override
    public Statistics getStatistics() {
        return this.statsManager;
    }
}
