package com.uroria.backend.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import de.leonhard.storage.Json;
import de.leonhard.storage.internal.settings.ReloadSettings;
import org.slf4j.Logger;

@Plugin(
        id = "backend",
        name = "Backend"
)
public final class BackendVelocityPlugin {
    private static final Json CONFIG = new Json("backend.json", "./", BackendVelocityPlugin.class.getClassLoader().getResourceAsStream("backend.json"), ReloadSettings.MANUALLY);;

    private final Logger logger;
    private final ProxyServer proxyServer;
    private final BackendAPI backendAPI;
    @Inject
    public BackendVelocityPlugin(Logger logger, ProxyServer proxyServer) {
        this.logger = logger;
        this.proxyServer = proxyServer;
        BackendAPI backendAPI;
        try {
            backendAPI = new BackendAPI(CONFIG.getString("pulsar.url"), CONFIG.getOrSetDefault("sentry.enabled", false), this.logger, this.proxyServer);
        } catch (Exception exception) {
            this.logger.error("Cannot connect to backend! Exiting...", exception);
            this.backendAPI = null;
            return;
        }
        this.backendAPI = backendAPI;
    }

    @Subscribe
    public void onProxyInitializeEvent(ProxyInitializeEvent proxyInitializeEvent) {
        this.backendAPI.start();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent proxyShutdownEvent) {
        this.backendAPI.shutdown();
    }

    static Json getConfig() {
        return CONFIG;
    }
}
