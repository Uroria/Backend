package com.uroria.backend.wrapper;

import com.uroria.backend.BackendWrapper;
import com.uroria.backend.Unsafe;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.impl.clan.ClanManager;
import com.uroria.backend.impl.permission.PermGroupManager;
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
import org.slf4j.Logger;

import java.util.Collection;
import java.util.UUID;

public final class Wrapper extends AbstractBackendWrapper implements BackendWrapper {

    private final UserManager userManager;
    private final PermGroupManager groupManager;
    private final ClanManager clanManager;
    private final ServerManager serverManager;

    @SuppressWarnings("ErrorMarkers")
    public Wrapper(Logger logger) throws Exception {
        super(logger);
        Unsafe.setInstance(this);
        this.userManager = new UserManager(getRabbit(), logger);
        this.groupManager = new PermGroupManager(getRabbit(), logger);
        this.clanManager = new ClanManager(getRabbit(), logger);
        this.serverManager = new ServerManager(getRabbit(), logger);
    }

    @Override
    public void start() throws Exception {
        this.userManager.start();
        this.groupManager.start();
        this.clanManager.start();
        this.serverManager.start();
    }

    @Override
    public void shutdown() throws Exception {
        this.userManager.shutdown();
        this.groupManager.shutdown();
        this.clanManager.shutdown();
        this.serverManager.shutdown();
        super.shutdown();
    }

    @Override
    public Result<User> getUser(UUID uuid) {
        try {
            return Result.of(this.userManager.getWrapper(uuid));
        } catch (Exception exception) {
            this.logger.error("Cannot request user with uuid " + uuid, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<User> getUser(String username) {
        try {
            return Result.of(this.userManager.getWrapper(username));
        } catch (Exception exception) {
            this.logger.error("Cannot request user with username " + username, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<Clan> getClan(String tag) {
        try {
            return Result.of(this.clanManager.getWrapper(tag));
        } catch (Exception exception) {
            this.logger.error("Cannot request clan with tag " + tag, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<Server> getServer(long identifier) {
        try {
            return Result.of(this.serverManager.getServer(identifier));
        } catch (Exception exception) {
            this.logger.error("Cannot request server with identifier " + identifier, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<Proxy> getProxy(long identifier) {
        return Result.none();
    }

    @Override
    public Collection<Proxy> getProxies(String name) {
        return ObjectSets.emptySet();
    }

    @Override
    public Result<ServerGroup> getServerGroup(String name) {
        return Result.none();
    }

    @Override
    public Result<PermGroup> getPermissionGroup(String name) {
        try {
            return Result.of(this.groupManager.getWrapper(name, false));
        } catch (Exception exception) {
            this.logger.error("Cannot request permission-group with name " + name, exception);
            return Result.problem(Problem.error(exception));
        }
    }

    @Override
    public Result<PermGroup> createPermissionGroup(String name) {
        try {
            return Result.of(this.groupManager.getWrapper(name, true));
        } catch (Exception exception) {
            this.logger.error("Cannot create permission-group with name " + name, exception);
            return Result.problem(Problem.error(exception));
        }
    }
}
