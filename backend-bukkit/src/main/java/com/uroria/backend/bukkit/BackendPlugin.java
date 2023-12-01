package com.uroria.backend.bukkit;

import com.uroria.are.Application;
import com.uroria.backend.Backend;
import com.uroria.backend.WrapperEnvironment;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.cache.communication.server.ServerPing;
import com.uroria.backend.communication.broadcast.Broadcaster;
import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.impl.BackendInitializer;
import com.uroria.backend.impl.BackendWrapperImpl;
import com.uroria.backend.impl.utils.WrapperUtils;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.server.events.ServerDeletedEvent;
import com.uroria.backend.server.events.ServerUpdatedEvent;
import com.uroria.base.event.EventManager;
import com.uroria.base.event.Listener;
import com.uroria.base.utils.ThreadUtils;
import com.uroria.problemo.result.Result;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class BackendPlugin extends JavaPlugin {
    private final Logger logger;
    private final AbstractBackendWrapper wrapper;
    private Server server;
    private Broadcaster<ServerPing> ping;

    public BackendPlugin() {
        try {
            this.wrapper = BackendInitializer.initialize();
            this.logger = this.wrapper.getLogger();
            WrapperUtils.getServerId(logger).ifPresentOrElse(id -> wrapper.getEnvironment().setServerId(id), () -> {
                logger.warn("No server-id found");
            });
            WrapperUtils.getTemplateId(logger).ifPresentOrElse(id -> wrapper.getEnvironment().setTemplateId(id), () -> {
                logger.warn("No template-id found");
            });
            WrapperUtils.getGroupName().ifPresentOrElse(name -> wrapper.getEnvironment().setServerGroupName(name), () -> {
                logger.warn("No group-name found");
            });
            this.wrapper.getEventManager().subscribe(new Listener<>(ServerUpdatedEvent.class, 1) {
                @Override
                public void onEvent(ServerUpdatedEvent event) {
                    if (server == null) return;
                    Server updatedServer = event.getServer();
                    if (updatedServer.getId() != server.getId()) return;
                    switch (updatedServer.getStatus()) {
                        case STOPPED -> {
                            Bukkit.shutdown();
                        }
                        case EMPTY -> {
                            if (wrapper.isStarted()) {
                                updatedServer.setStatus(ApplicationStatus.STARTING);
                                return;
                            }
                            updatedServer.setStatus(ApplicationStatus.STOPPED);
                            Bukkit.shutdown();
                        }
                    }
                }
            });
        } catch (Exception exception) {
            LoggerFactory.getLogger("BackendEmergency").error("Unable to initialize", exception);
            Bukkit.shutdown();
            throw exception;
        }
    }

    @Override
    public void onLoad() {
        try {
            this.wrapper.start();
        } catch (Exception exception) {
            this.logger.error("Cannot start backend", exception);
            Bukkit.shutdown();
        }
    }

    @SuppressWarnings("WarningMarkers")
    @Override
    public void onEnable() {
        try {
            if (!this.wrapper.isStarted())
                throw new IllegalStateException("Wrapper was never started or plugin has been illegally reloaded");
            getServer().getPluginManager().registerEvents(new Listeners(this.wrapper), this);
            if (Application.isOffline()) {
                this.logger.warn("Running in offline mode. No connections will be established");
                serverSetupIfOffline();
                return;
            }
            WrapperEnvironment environment = Backend.environment();
            this.server = environment.getServer().orElse(null);
            if (this.server == null) {
                int templateId = environment.getTemplateId().orElseThrow(() -> new IllegalStateException("Template and server id were never assigned even if one of them has to"));
                String groupName = environment.getServerGroupName().orElseThrow(() -> new IllegalStateException("Group and server id were never assigned even if one of them has to beside template-id"));
                ServerGroup group = Backend.serverGroup(groupName).get();
                if (group == null) {
                    group = Backend.wrapper().createServerGroup(groupName, 999).get();
                    if (group == null) throw new IllegalStateException("Unable to create server-group");
                }
                Result<Server> result = Backend.wrapper().createServer(templateId, group);
                if (result instanceof Result.Problematic<Server> problematic) {
                    throw new RuntimeException(problematic.getProblem().getError().orElseThrow(() -> new IllegalStateException("Some problem while trying to create server?")));
                }
                this.server = result.get();
                if (this.server == null) throw new IllegalStateException("Unable to create server");
            }
            EventManager eventManager = this.wrapper.getEventManager();
            eventManager.subscribe(new Listener<>(ServerUpdatedEvent.class, 0) {
                @Override
                public void onEvent(ServerUpdatedEvent event) {
                    long id = event.getServer().getId();
                    if (server.getId() != id) return;
                    if (event.getServer().getStatus() == ApplicationStatus.STOPPED) Bukkit.shutdown();
                }
            });
            eventManager.subscribe(new Listener<>(ServerDeletedEvent.class, 0) {
                @Override
                public void onEvent(ServerDeletedEvent event) {
                    long id = event.getServer().getId();
                    if (server.getId() == id) Bukkit.shutdown();
                }
            });
            if (wrapper instanceof BackendWrapperImpl onlineWrapper) {
                this.ping = onlineWrapper.getServerManager().getBroadcastPoint().registerBroadcaster(ServerPing.class, "Ping");
                CompletableFuture.runAsync(() -> {
                    while (true) {
                        if (this.server.getStatus() == ApplicationStatus.STOPPED || this.server.isDeleted()) {
                            ping.broadcast(new ServerPing(this.server.getId(), System.currentTimeMillis(), true));
                            return;
                        }
                        ping.broadcast(new ServerPing(this.server.getId(), System.currentTimeMillis(), false));
                        ThreadUtils.sleep(1, TimeUnit.SECONDS);
                    }
                });
            }
            this.server.setStatus(ApplicationStatus.ONLINE);
        } catch (Exception exception) {
            this.logger.error("Cannot enable backend", exception);
            Bukkit.shutdown();
        }
    }

    @SuppressWarnings("WarningMarkers")
    private void serverSetupIfOffline() {
        this.server = Backend.environment().getServer().orElse(null);
        if (server == null) {
            ServerGroup group = Backend.serverGroup("offline").get();
            if (group == null) group = Backend.wrapper().createServerGroup("offline", 999).get();
            if (group == null) throw new IllegalStateException("Group still null");
            server = Backend.wrapper().createServer(0, group).get();
            if (server == null) throw new RuntimeException("Unable to setup server");
        }
        this.server.setStatus(ApplicationStatus.ONLINE);
    }

    @Override
    public void onDisable() {
        try {
            if (this.server != null) {
                this.server.delete();
                if (this.ping != null) {
                    this.ping.broadcast(new ServerPing(this.server.getId(), System.currentTimeMillis(), true));
                }
            }
            this.wrapper.shutdown();
        } catch (Exception exception) {
            this.logger.error("Cannot shutdown backend", exception);
            Bukkit.shutdown();
        }
    }
}
