package com.uroria.backend.pluginapi.plugins;

import com.uroria.backend.pluginapi.Server;
import com.uroria.backend.pluginapi.events.EventManager;
import com.uroria.backend.pluginapi.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BackendPlugin {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private Server server;
    private PluginManager pluginManager;
    private EventManager eventManager;
    private Scheduler scheduler;
    private PluginConfiguration pluginConfiguration;

    public BackendPlugin() {}

    void setFields(Server server, PluginConfiguration pluginConfiguration) {
        this.server = server;
        this.pluginConfiguration = pluginConfiguration;
        this.pluginManager = this.server.getPluginManager();
        this.eventManager = this.server.getEventManager();
        this.scheduler = this.server.getScheduler();
    }

    public abstract void start();

    public abstract void stop();

    public final Server getServer() {
        return server;
    }

    public final PluginManager getPluginManager() {
        return pluginManager;
    }

    public final EventManager getEventManager() {
        return eventManager;
    }

    public final Scheduler getScheduler() {
        return scheduler;
    }

    public final String getPluginName() {
        return this.pluginConfiguration.getPluginName();
    }

    public final String getPluginVersion() {
        return this.pluginConfiguration.getVersion();
    }

    public final Logger getLogger() {
        return this.logger;
    }

    final PluginConfiguration getPluginConfiguration() {
        return this.pluginConfiguration;
    }
}
