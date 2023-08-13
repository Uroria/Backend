package com.uroria.backend.velocity;

import com.uroria.backend.Backend;
import com.uroria.backend.Unsafe;
import com.uroria.backend.impl.AbstractBackend;
import com.uroria.backend.impl.root.BackendRequestChannel;
import com.uroria.backend.impl.root.StopUpdateChannel;
import com.uroria.backend.message.MessageManager;
import com.uroria.backend.punishment.PunishmentManager;
import com.uroria.backend.stats.StatsManager;
import com.uroria.backend.velocity.clan.ClanManagerImpl;
import com.uroria.backend.velocity.friend.FriendManagerImpl;
import com.uroria.backend.velocity.message.MessageManagerImpl;
import com.uroria.backend.velocity.permission.PermManagerImpl;
import com.uroria.backend.velocity.punishment.PunishmentManagerImpl;
import com.uroria.backend.velocity.server.ServerManagerImpl;
import com.uroria.backend.velocity.twitch.TwitchManagerImpl;
import com.uroria.backend.velocity.user.UserManagerImpl;
import com.velocitypowered.api.proxy.ProxyServer;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.UUID;

public final class BackendImpl extends AbstractBackend implements Backend {
    private static BackendImpl instance;

    private final Logger logger;
    private final ProxyServer proxyServer;
    private final UserManagerImpl userManager;
    private final TwitchManagerImpl twitchManager;
    private final PermManagerImpl permManager;
    private final ServerManagerImpl serverManager;
    private final ClanManagerImpl clanManager;
    private final FriendManagerImpl friendManager;
    private final PunishmentManagerImpl punishmentManager;
    private final MessageManagerImpl messageManager;

    private BackendRequestChannel request;
    private StopUpdateChannel stopAll;

    @SuppressWarnings("deprecation")
    BackendImpl(String pulsarURL, Logger logger, ProxyServer proxyServer) {
        super(pulsarURL);
        this.proxyServer = proxyServer;
        instance = this;
        this.logger = logger;
        Unsafe.setInstance(this);
        this.userManager = new UserManagerImpl(getPulsarClient(), logger, proxyServer);
        this.twitchManager = new TwitchManagerImpl(getPulsarClient(), logger, proxyServer);
        this.permManager = new PermManagerImpl(getPulsarClient(), logger, proxyServer);
        this.serverManager = new ServerManagerImpl(getPulsarClient(), logger, proxyServer);
        this.friendManager = new FriendManagerImpl(getPulsarClient(), logger, proxyServer);
        this.clanManager = new ClanManagerImpl(getPulsarClient(), logger, proxyServer);
        this.punishmentManager = new PunishmentManagerImpl(getPulsarClient(), logger, proxyServer);
        this.messageManager = new MessageManagerImpl(getPulsarClient(), logger, proxyServer);
    }

    @Override
    public void start() throws PulsarClientException {
        if (BackendVelocityPlugin.isOffline()) return;
        this.logger.info("Starting connections...");
        String identifier = UUID.randomUUID().toString();

        this.request = new BackendRequestChannel(getPulsarClient(), identifier);

        if (!isOnline()) return;

        this.stopAll = new StopUpdateChannel(getPulsarClient(), identifier, this::stop);

        this.userManager.start(identifier);
        this.twitchManager.start(identifier);
        this.permManager.start(identifier);
        this.serverManager.start(identifier);
        this.friendManager.start(identifier);
        this.clanManager.start(identifier);
        this.punishmentManager.start(identifier);
        this.messageManager.start(identifier);
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (BackendVelocityPlugin.isOffline()) return;
        this.logger.info("Shutting down connections...");
        this.userManager.shutdown();
        this.twitchManager.shutdown();
        this.permManager.shutdown();
        this.serverManager.shutdown();
        this.friendManager.shutdown();
        this.clanManager.shutdown();
        this.punishmentManager.shutdown();
        this.messageManager.shutdown();
        if (this.request != null) this.request.close();
        if (this.stopAll != null) this.stopAll.close();
        super.shutdown();
    }

    private void stop() {
        this.logger.warn("RECEIVED ROOT LEVEL SHUTDOWN. EVERYTHING WILL CLOSE!");
        this.proxyServer.shutdown();
    }

    @Override
    public PermManagerImpl getPermissionManager() {
        return this.permManager;
    }

    @Override
    public TwitchManagerImpl getTwitchManager() {
        return this.twitchManager;
    }

    @Override
    public UserManagerImpl getUserManager() {
        return this.userManager;
    }

    @Override
    public FriendManagerImpl getFriendManager() {
        return this.friendManager;
    }

    @Override
    public ClanManagerImpl getClanManager() {
        return this.clanManager;
    }

    @Override
    public StatsManager getStatsManager() {
        return null;
    }

    @Override
    public ServerManagerImpl getServerManager() {
        return this.serverManager;
    }

    @Override
    public PunishmentManagerImpl getPunishmentManager() {
        return this.punishmentManager;
    }

    @Override
    public MessageManagerImpl getMessageManager() {
        return this.messageManager;
    }

    @Override
    public void stopEverything() {
        this.stopAll.update(true);
    }

    @Override
    public boolean isOnline() {
        return this.request.request(System.currentTimeMillis(), 10000).orElse(false);
    }

    public static BackendImpl getAPI() {
        return instance;
    }
}
