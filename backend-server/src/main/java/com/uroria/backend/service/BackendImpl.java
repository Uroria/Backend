package com.uroria.backend.service;

import com.mongodb.client.MongoDatabase;
import com.uroria.backend.Unsafe;
import com.uroria.backend.clan.ClanManager;
import com.uroria.backend.friend.FriendManager;
import com.uroria.backend.impl.AbstractBackend;
import com.uroria.backend.punishment.PunishmentManager;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.service.modules.clan.BackendClanManager;
import com.uroria.backend.service.modules.permission.BackendPermManager;
import com.uroria.backend.service.modules.server.BackendServerManager;
import com.uroria.backend.service.modules.user.BackendUserManager;
import com.uroria.backend.stats.StatsManager;
import com.uroria.backend.twitch.TwitchManager;
import io.lettuce.core.api.StatefulRedisConnection;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

public final class BackendImpl extends AbstractBackend {
    private static final Logger logger = BackendServer.getLogger();

    private final BackendUserManager userManager;
    private final BackendPermManager permManager;
    private final BackendClanManager clanManager;
    private final BackendServerManager serverManager;

    @SuppressWarnings("deprecation")
    public BackendImpl(PulsarClient pulsarClient, MongoDatabase database, StatefulRedisConnection<String, String> redis) {
        super(pulsarClient);
        Unsafe.setInstance(this);
        this.userManager = new BackendUserManager(getPulsarClient(), database, redis);
        this.permManager = new BackendPermManager(getPulsarClient(), database, redis);
        this.clanManager = new BackendClanManager(getPulsarClient(), database, redis);
        this.serverManager = new BackendServerManager(getPulsarClient());
    }

    @Override
    public void start() {
        this.userManager.start();
        this.permManager.start();
        this.clanManager.start();
        this.serverManager.start();
    }

    @Override
    public void shutdown() throws PulsarClientException {
        this.userManager.shutdown();
        this.permManager.shutdown();
        this.clanManager.shutdown();
        this.serverManager.shutdown();
        super.shutdown();
    }

    @Override
    public void stopEverything() {

    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public BackendPermManager getPermissionManager() {
        return this.permManager;
    }

    @Override
    public TwitchManager getTwitchManager() {
        return null;
    }

    @Override
    public BackendUserManager getUserManager() {
        return this.userManager;
    }

    @Override
    public FriendManager getFriendManager() {
        return null;
    }

    @Override
    public ClanManager getClanManager() {
        return null;
    }

    @Override
    public StatsManager getStatsManager() {
        return null;
    }

    @Override
    public ServerManager getServerManager() {
        return this.serverManager;
    }

    @Override
    public PunishmentManager getPunishmentManager() {
        return null;
    }
}
