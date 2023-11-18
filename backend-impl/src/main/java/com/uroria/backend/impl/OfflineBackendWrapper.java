package com.uroria.backend.impl;

import com.uroria.backend.Backend;
import com.uroria.backend.Unsafe;
import com.uroria.backend.clan.Clan;
import com.uroria.backend.impl.clan.OfflineClanManager;
import com.uroria.backend.impl.user.OfflineUserManager;
import com.uroria.backend.permission.PermGroup;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.stats.Statistics;
import com.uroria.backend.user.User;
import com.uroria.base.scheduler.Scheduler;
import com.uroria.base.scheduler.SchedulerFactory;
import com.uroria.problemo.result.Result;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.NonNull;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.UUID;

public final class OfflineBackendWrapper extends AbstractBackendWrapper implements Backend {
    private final Scheduler scheduler;
    private final OfflineClanManager clanManager;
    private final OfflineUserManager userManager;

    @SuppressWarnings("ErrorMarkers")
    OfflineBackendWrapper(Logger logger) {
        super(logger);
        Unsafe.setInstance(this);
        this.scheduler = SchedulerFactory.create("Scheduler");
        this.clanManager = new OfflineClanManager();
        this.userManager = new OfflineUserManager();
    }

    @Override
    public void start() throws Exception {
        if (this.started) return;
        this.started = true;
    }

    @Override
    public void shutdown() throws Exception {

    }

    @Override
    public Result<User> getUser(UUID uuid) {
        return Result.of(this.userManager.getUser(uuid));
    }

    @Override
    public Result<User> getUser(String username) {
        return Result.of(this.userManager.getUser(username));
    }

    @Override
    public Result<User> getUser(long discordUserId) {
        return Result.none();
    }

    @Override
    public Result<Clan> getClan(String tag) {
        return Result.of(this.clanManager.getClan(tag));
    }

    @Override
    public Result<Clan> createClan(@NonNull String name, @NonNull String tag, @NonNull User operator, long foundingDate) {
        return Result.of(this.clanManager.createClan(tag, name, operator, foundingDate));
    }

    @Override
    public Result<Server> getServer(long identifier) {
        return Result.none();
    }

    @Override
    public Collection<Server> getServers() {
        return ObjectSets.emptySet();
    }

    @Override
    public Result<Proxy> getProxy(long identifier) {
        return Result.none();
    }

    @Override
    public Result<Proxy> createProxy(String name, int templateId, int maxPlayers) {
        return Result.none();
    }

    @Override
    public Collection<Proxy> getProxies(String name) {
        return ObjectSets.emptySet();
    }

    @Override
    public Collection<Proxy> getProxies() {
        return ObjectSets.emptySet();
    }

    @Override
    public Result<ServerGroup> getServerGroup(String type) {
        return Result.none();
    }

    @Override
    public Collection<ServerGroup> getServerGroups() {
        return ObjectSets.emptySet();
    }

    @Override
    public Result<ServerGroup> createServerGroup(String name, int maxPlayers) {
        return Result.none();
    }

    @Override
    public Result<PermGroup> getPermissionGroup(String name) {
        return Result.none();
    }

    @Override
    public Collection<PermGroup> getPermissionGroups() {
        return ObjectSets.emptySet();
    }

    @Override
    public Result<PermGroup> createPermissionGroup(String name) {
        return Result.none();
    }

    @Override
    public Statistics getStatistics() {
        return null;
    }

    @Override
    public Scheduler getScheduler() {
        return this.scheduler;
    }
}
