package com.uroria.backend.wrapper;

import com.uroria.backend.BackendWrapper;
import com.uroria.backend.Unsafe;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.impl.clan.ClanManager;
import com.uroria.backend.impl.clan.ClanWrapper;
import com.uroria.backend.impl.permission.PermGroupManager;
import com.uroria.backend.impl.proxy.ProxyManager;
import com.uroria.backend.impl.server.ServerGroupManager;
import com.uroria.backend.impl.server.ServerManager;
import com.uroria.backend.impl.user.UserManager;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.user.User;
import com.uroria.problemo.Problem;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.NonNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.UUID;

public final class Wrapper extends AbstractBackendWrapper implements BackendWrapper {

    private final UserManager userManager;
    private final PermGroupManager groupManager;
    private final ClanManager clanManager;
    private final ServerManager serverManager;
    private final ServerGroupManager serverGroupManager;
    private final ProxyManager proxyManager;

    @SuppressWarnings("ErrorMarkers")
    public Wrapper(Logger logger) throws Exception {
        super(logger);
        Unsafe.setInstance(this);
        this.userManager = new UserManager(getRabbit());
        this.groupManager = new PermGroupManager(getRabbit());
        this.clanManager = new ClanManager(getRabbit());
        this.serverManager = new ServerManager(getRabbit());
        this.serverGroupManager = new ServerGroupManager(getRabbit());
        this.proxyManager = new ProxyManager(getRabbit());
    }

    @Override
    public void start() throws Exception {
        this.userManager.start();
        this.groupManager.start();
        this.clanManager.start();
        this.proxyManager.start();
        this.serverGroupManager.start();
        this.serverManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.userManager.shutdown();
        this.groupManager.shutdown();
        this.clanManager.shutdown();
        this.serverManager.shutdown();
        this.serverGroupManager.shutdown();
        this.proxyManager.shutdown();
        super.shutdown();
    }

    @Override
    public Result<User> getUser(UUID uuid) {
        try {
            return Result.of(this.userManager.getUserWrapper(uuid));
        } catch (Exception exception) {
            this.logger.error("Cannot request user with uuid " + uuid, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<User> getUser(String username) {
        try {
            return Result.of(this.userManager.getUserWrapper(username));
        } catch (Exception exception) {
            this.logger.error("Cannot request user with username " + username, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<Clan> getClan(String tag) {
        try {
            return Result.of(this.clanManager.getClanWrapper(tag));
        } catch (Exception exception) {
            this.logger.error("Cannot request clan with tag " + tag, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator, long foundingDate) {
        try {
            ClanWrapper clan = this.clanManager.createClanWrapper(name, tag, foundingDate);
            operator.joinClan(clan);
            clan.addOperator(operator);
            return Result.of(clan);
        } catch (Exception exception) {
            this.logger.error("Cannot create clan with tag " + tag, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<Server> getServer(long identifier) {
        try {
            return Result.of(this.serverManager.getServerWrapper(identifier));
        } catch (Exception exception) {
            this.logger.error("Cannot request server with identifier " + identifier, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Collection<Server> getServers() {
        try {
            return this.serverManager.getServers();
        } catch (Exception exception) {
            this.logger.error("Cannot request all server-groups", exception);
            return ObjectSets.emptySet();
        }
    }

    @Override
    public Result<Proxy> getProxy(long identifier) {
        try {
            return Result.of(this.proxyManager.getProxyWrapper(identifier));
        } catch (Exception exception) {
            this.logger.error("Cannot request proxy with identifier " + identifier, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<Proxy> createProxy(String name, int maxPlayers) {
        return null;
    }

    @Override
    public Collection<Proxy> getProxies(String name) {
        return ObjectSets.emptySet();
    }

    @Override
    public Collection<Proxy> getProxies() {
        return null;
    }

    @Override
    public Result<ServerGroup> getServerGroup(String name) {
        try {
            return Result.of(this.serverGroupManager.getGroupWrapper(name));
        } catch (Exception exception) {
            this.logger.error("Cannot request server-group with name " + name, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Collection<ServerGroup> getServerGroups() {
        try {
            return this.serverGroupManager.getGroups();
        } catch (Exception exception) {
            this.logger.error("Cannot request all server-groups", exception);
            return ObjectSets.emptySet();
        }
    }

    @Override
    public Result<ServerGroup> createServerGroup(String name, int maxPlayers) {
        try {
            return Result.of(this.serverGroupManager.createGroupWrapper(name, maxPlayers));
        } catch (Exception exception) {
            this.logger.error("Cannot request server-group creation with name " + name, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<PermGroup> getPermissionGroup(String name) {
        try {
            return Result.of(this.groupManager.getGroup(name));
        } catch (Exception exception) {
            this.logger.error("Cannot request permission-group with name " + name, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Collection<PermGroup> getPermissionGroups() {
        try {
            return this.groupManager.getGroups();
        } catch (Exception exception) {
            this.logger.error("Cannot request all perm-groups", exception);
            return ObjectSets.emptySet();
        }
    }

    @Override
    public Result<PermGroup> createPermissionGroup(String name) {
        try {
            return Result.of(this.groupManager.createGroup(name));
        } catch (Exception exception) {
            this.logger.error("Cannot create permission-group with name " + name, exception);
            return Result.problem(Problem.error(exception));
        }
    }
}
