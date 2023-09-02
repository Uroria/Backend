package com.uroria.backend.service;

import com.mongodb.client.MongoDatabase;
import com.uroria.backend.Unsafe;
import com.uroria.backend.friend.FriendManager;
import com.uroria.backend.impl.AbstractBackend;
import com.uroria.backend.service.modules.clan.BackendClanManager;
import com.uroria.backend.service.modules.message.BackendMessageManager;
import com.uroria.backend.service.modules.permission.BackendPermManager;
import com.uroria.backend.service.modules.punishment.BackendPunishmentManager;
import com.uroria.backend.service.modules.root.BackendRootManager;
import com.uroria.backend.service.modules.server.BackendServerManager;
import com.uroria.backend.service.modules.user.BackendUserManager;
import com.uroria.backend.stats.StatsManager;
import com.uroria.backend.twitch.TwitchManager;
import com.uroria.base.event.EventManager;
import com.uroria.base.event.EventManagerFactory;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.Getter;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

public final class BackendImpl extends AbstractBackend {
    private static final Logger logger = BackendServer.getLogger();

    private final EventManager eventManager;
    private final BackendServer server;
    private final BackendUserManager userManager;
    private final BackendPermManager permManager;
    private final BackendClanManager clanManager;
    private final BackendServerManager serverManager;
    private final BackendPunishmentManager punishmentManager;
    private final BackendMessageManager messageManager;
    private @Getter final BackendRootManager rootManager;

    @SuppressWarnings("deprecation")
    public BackendImpl(PulsarClient pulsarClient, BackendServer server, MongoDatabase database, StatefulRedisConnection<String, String> redis) {
        super(pulsarClient);
        Unsafe.setInstance(this);
        this.server = server;
        this.eventManager = EventManagerFactory.create("Backend");
        this.userManager = new BackendUserManager(getPulsarClient(), database, redis);
        this.permManager = new BackendPermManager(getPulsarClient(), database, redis);
        this.clanManager = new BackendClanManager(getPulsarClient(), database, redis);
        this.serverManager = new BackendServerManager(getPulsarClient());
        this.punishmentManager = new BackendPunishmentManager(getPulsarClient(), database, redis);
        this.rootManager = new BackendRootManager(getPulsarClient(), this.server);
        this.messageManager = new BackendMessageManager(getPulsarClient());
    }

    @Override
    public void start() {
        this.rootManager.start();
        this.userManager.start();
        this.permManager.start();
        this.clanManager.start();
        this.serverManager.start();
        this.punishmentManager.start();
        this.messageManager.start();
    }

    @Override
    public void shutdown() throws PulsarClientException {
        this.userManager.shutdown();
        this.permManager.shutdown();
        this.clanManager.shutdown();
        this.serverManager.shutdown();
        this.punishmentManager.shutdown();
        this.rootManager.shutdown();
        this.messageManager.shutdown();
        super.shutdown();
    }

    @Override
    public void stopEverything() {
        this.rootManager.shutdownAll();
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
    public BackendClanManager getClanManager() {
        return this.clanManager;
    }

    @Override
    public StatsManager getStatsManager() {
        return null;
    }

    @Override
    public BackendServerManager getServerManager() {
        return this.serverManager;
    }

    @Override
    public BackendPunishmentManager getPunishmentManager() {
        return this.punishmentManager;
    }

    @Override
    public BackendMessageManager getMessageManager() {
        return this.messageManager;
    }

    @Override
    public EventManager getEventManager() {
        return null;
    }
}
