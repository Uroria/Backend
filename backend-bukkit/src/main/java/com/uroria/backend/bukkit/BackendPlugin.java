package com.uroria.backend.bukkit;

import com.uroria.are.Application;
import com.uroria.backend.Backend;
import com.uroria.backend.WrapperEnvironment;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.impl.BackendInitializer;
import com.uroria.backend.impl.utils.WrapperUtils;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerGroup;
import com.uroria.backend.server.events.ServerUpdatedEvent;
import com.uroria.base.event.Listener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class BackendPlugin extends JavaPlugin {
    private final Logger logger;
    private final AbstractBackendWrapper wrapper;
    private Server server;

    public BackendPlugin() {
        try {
            this.wrapper = BackendInitializer.initialize();
            this.logger = this.wrapper.getLogger();
            WrapperUtils.getServerId(logger).ifPresent(id -> wrapper.getEnvironment().setServerId(id));
            WrapperUtils.getTemplateId(logger).ifPresent(id -> wrapper.getEnvironment().setTemplateId(id));
            WrapperUtils.getGroupName().ifPresent(name -> wrapper.getEnvironment().setServerGroupName(name));
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
            WrapperEnvironment environment = Backend.getEnvironment();
            this.server = environment.getServer().orElse(null);
            if (this.server == null) {
                int templateId = environment.getTemplateId().orElseThrow(() -> new IllegalStateException("Template and server id were never assigned even if one of them has to"));
                String groupName = environment.getServerGroupName().orElseThrow(() -> new IllegalStateException("Group and server id were never assigned even if one of them has to beside template-id"));
                ServerGroup group = Backend.getServerGroup(groupName).get();
                if (group == null) {
                    group = Backend.createServerGroup(groupName, 999).get();
                    if (group == null) throw new IllegalStateException("Unable to create server-group");
                }
                this.server = Backend.createServer(templateId, group).get();
                if (this.server == null) throw new IllegalStateException("Unable to create server");
            }
            this.server.setStatus(ApplicationStatus.ONLINE);
        } catch (Exception exception) {
            this.logger.error("Cannot enable backend", exception);
            Bukkit.shutdown();
        }
    }

    @SuppressWarnings("WarningMarkers")
    private void serverSetupIfOffline() {
        this.server = Backend.getEnvironment().getServer().orElse(null);
        if (server == null) {
            ServerGroup group = Backend.getServerGroup("offline").get();
            if (group == null) group = Backend.createServerGroup("offline", 999).get();
            server = Backend.createServer(0, group).get();
        }
        this.server.setStatus(ApplicationStatus.ONLINE);
    }

    @Override
    public void onDisable() {
        try {
            if (this.server != null) {
                this.server.delete();
            }
            this.wrapper.shutdown();
        } catch (Exception exception) {
            this.logger.error("Cannot shutdown backend", exception);
            Bukkit.shutdown();
        }
    }
}
