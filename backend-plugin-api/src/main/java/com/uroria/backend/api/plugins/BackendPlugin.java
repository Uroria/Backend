package com.uroria.backend.api.plugins;

import com.uroria.backend.api.Server;
import com.uroria.backend.api.events.EventManager;
import com.uroria.backend.api.scheduler.Scheduler;
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

    private BackendPlugin(Server server, PluginConfiguration pluginConfiguration) {
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
