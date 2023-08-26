package com.uroria.backend.bukkit;

import com.uroria.backend.Backend;
import com.uroria.backend.Unsafe;
import com.uroria.backend.bukkit.clan.ClanManagerImpl;
import com.uroria.backend.bukkit.friend.FriendManagerImpl;
import com.uroria.backend.bukkit.message.MessageManagerImpl;
import com.uroria.backend.bukkit.permission.PermManagerImpl;
import com.uroria.backend.bukkit.punishment.PunishmentManagerImpl;
import com.uroria.backend.bukkit.server.ServerManagerImpl;
import com.uroria.backend.bukkit.stat.StatsManagerImpl;
import com.uroria.backend.bukkit.twitch.TwitchManagerImpl;
import com.uroria.backend.bukkit.user.UserManagerImpl;
import com.uroria.backend.impl.AbstractBackend;
import com.uroria.backend.impl.root.BackendRequestChannel;
import com.uroria.backend.impl.root.StopUpdateChannel;
import com.uroria.backend.message.MessageManager;
import com.uroria.backend.punishment.PunishmentManager;
import com.uroria.backend.utils.ThreadUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.bukkit.Bukkit;
import org.slf4j.Logger;

import java.util.UUID;

public final class BackendImpl extends AbstractBackend implements Backend {
    private static BackendImpl instance;

    private final Logger logger;
    private final UserManagerImpl userManager;
    private final TwitchManagerImpl twitchManager;
    private final PermManagerImpl permManager;
    private final ServerManagerImpl serverManager;
    private final ClanManagerImpl clanManager;
    private final FriendManagerImpl friendManager;
    private final StatsManagerImpl statsManager;
    private final PunishmentManagerImpl punishmentManager;
    private final MessageManagerImpl messageManager;

    private BackendRequestChannel request;
    private StopUpdateChannel stopAll;

    @SuppressWarnings("deprecation")
    BackendImpl(String pulsarURL, Logger logger) {
        super(pulsarURL);
        instance = this;
        this.logger = logger;
        Unsafe.setInstance(this);
        this.userManager = new UserManagerImpl(getPulsarClient(), logger);
        this.twitchManager = new TwitchManagerImpl(getPulsarClient(), logger);
        this.permManager = new PermManagerImpl(getPulsarClient(), logger);
        this.serverManager = new ServerManagerImpl(getPulsarClient(), logger);
        this.friendManager = new FriendManagerImpl(getPulsarClient(), logger);
        this.clanManager = new ClanManagerImpl(getPulsarClient(), logger);
        this.statsManager = new StatsManagerImpl(getPulsarClient(), logger);
        this.punishmentManager = new PunishmentManagerImpl(getPulsarClient(), logger);
        this.messageManager = new MessageManagerImpl(getPulsarClient(), logger);
    }

    @Override
    public void start() throws PulsarClientException {
        if (BackendBukkitPlugin.isOffline()) return;
        this.logger.info("Starting connections...");
        String identifier = UUID.randomUUID().toString();

        this.request = new BackendRequestChannel(getPulsarClient(), identifier);

        if (!isOnline()) {
            logger.warn("Backend not online! Shutting down.");
            Bukkit.shutdown();
            return;
        }

        this.stopAll = new StopUpdateChannel(getPulsarClient(), identifier, this::stop);

        this.userManager.start(identifier);
        this.twitchManager.start(identifier);
        this.permManager.start(identifier);
        this.serverManager.start(identifier);
        this.friendManager.start(identifier);
        this.clanManager.start(identifier);
        this.statsManager.start(identifier);
        this.punishmentManager.start(identifier);
        this.messageManager.start(identifier);
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (BackendBukkitPlugin.isOffline()) return;
        this.logger.info("Shutting down connections...");
        ThreadUtils.sleep(200);
        this.userManager.shutdown();
        ThreadUtils.sleep(200);
        this.twitchManager.shutdown();
        ThreadUtils.sleep(200);
        this.permManager.shutdown();
        ThreadUtils.sleep(200);
        this.serverManager.shutdown();
        ThreadUtils.sleep(200);
        this.friendManager.shutdown();
        ThreadUtils.sleep(200);
        this.clanManager.shutdown();
        ThreadUtils.sleep(200);
        this.statsManager.shutdown();
        ThreadUtils.sleep(200);
        this.punishmentManager.shutdown();
        ThreadUtils.sleep(200);
        this.messageManager.shutdown();
        ThreadUtils.sleep(200);
        if (this.request != null) this.request.close();
        if (this.stopAll != null) this.stopAll.close();
        ThreadUtils.sleep(200);
        super.shutdown();
    }

    private void stop() {
        this.logger.warn("RECEIVED ROOT LEVEL SHUTDOWN. EVERYTHING WILL CLOSE!");
        Bukkit.shutdown();
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
    public StatsManagerImpl getStatsManager() {
        return this.statsManager;
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
