package com.uroria.backend.wrapper;

import com.uroria.backend.Backend;
import com.uroria.backend.Unsafe;
import com.uroria.backend.impl.AbstractBackend;
import com.uroria.backend.impl.root.BackendRequestChannel;
import com.uroria.backend.impl.root.StopUpdateChannel;
import com.uroria.backend.stats.StatsManager;
import com.uroria.backend.wrapper.clan.ClanManagerImpl;
import com.uroria.backend.wrapper.friend.FriendManagerImpl;
import com.uroria.backend.wrapper.message.MessageManagerImpl;
import com.uroria.backend.wrapper.permission.PermManagerImpl;
import com.uroria.backend.wrapper.punishment.PunishmentManagerImpl;
import com.uroria.backend.wrapper.server.ServerManagerImpl;
import com.uroria.backend.wrapper.twitch.TwitchManagerImpl;
import com.uroria.backend.wrapper.user.UserManagerImpl;
import com.uroria.base.event.EventManager;
import com.uroria.base.event.EventManagerFactory;
import com.uroria.base.utils.ThreadUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.function.Function;

public final class BackendWrapper extends AbstractBackend implements Backend {
    private static BackendWrapper instance;

    private final Logger logger;
    private final EventManager eventManager;
    private final boolean offline;
    private final Runnable shutdown;
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
    public BackendWrapper(String pulsarURL, Logger logger, boolean offline, Runnable shutdown, Function<UUID, Boolean> onlinePlayerCheck) {
        super(pulsarURL);
        this.offline = offline;
        this.shutdown = shutdown;
        instance = this;
        this.logger = logger;
        Unsafe.setInstance(this);
        this.eventManager = EventManagerFactory.create("Backend");
        this.userManager = new UserManagerImpl(getPulsarClient(), logger, onlinePlayerCheck, offline, eventManager);
        this.twitchManager = new TwitchManagerImpl(getPulsarClient(), logger, onlinePlayerCheck, offline, eventManager);
        this.permManager = new PermManagerImpl(getPulsarClient(), logger, onlinePlayerCheck, offline, eventManager);
        this.serverManager = new ServerManagerImpl(getPulsarClient(), logger, offline, eventManager);
        this.friendManager = new FriendManagerImpl(getPulsarClient(), logger, onlinePlayerCheck, offline, eventManager);
        this.clanManager = new ClanManagerImpl(getPulsarClient(), logger, onlinePlayerCheck, offline, eventManager);
        this.punishmentManager = new PunishmentManagerImpl(getPulsarClient(), logger, onlinePlayerCheck, offline, eventManager);
        this.messageManager = new MessageManagerImpl(getPulsarClient(), logger, eventManager);
    }

    @Override
    public void start() throws PulsarClientException {
        if (this.offline) return;
        this.logger.info("Starting connections...");
        String identifier = UUID.randomUUID().toString();

        this.request = new BackendRequestChannel(getPulsarClient(), identifier);

        this.logger.info("Checking backend status...");
        if (!isOnline()) {
            this.logger.warn("Backend not online! Shutting down.");
            this.shutdown.run();
            return;
        }

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
        if (this.offline) return;
        this.logger.info("Shutting down connections...");
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
        this.punishmentManager.shutdown();
        ThreadUtils.sleep(200);
        this.messageManager.shutdown();
        ThreadUtils.sleep(200);
        if (this.request != null) this.request.close();
        ThreadUtils.sleep(200);
        if (this.stopAll != null) this.stopAll.close();
        ThreadUtils.sleep(200);
        super.shutdown();
    }

    private void stop() {
        this.logger.warn("RECEIVED ROOT LEVEL SHUTDOWN. EVERYTHING WILL CLOSE!");
        this.shutdown.run();
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
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Override
    public void stopEverything() {
        this.stopAll.update(true);
    }

    @Override
    public boolean isOnline() {
        return this.request.request(System.currentTimeMillis(), 10000).orElse(false);
    }

    public static BackendWrapper getAPI() {
        return instance;
    }
}
