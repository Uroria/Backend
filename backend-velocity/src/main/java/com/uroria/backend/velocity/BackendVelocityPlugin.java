package com.uroria.backend.velocity;

import com.google.inject.Inject;
import com.uroria.backend.impl.configuration.BackendConfiguration;
import com.uroria.backend.velocity.listeners.PlayerLogin;
import com.uroria.backend.wrapper.BackendWrapper;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;

@Plugin(
        id = "backend",
        name = "Backend",
        authors = "Verklickt"
)
public final class BackendVelocityPlugin {
    private final Logger logger;
    private final ProxyServer proxyServer;
    private @Getter final BackendWrapper backend;

    @Inject
    public BackendVelocityPlugin(Logger logger, ProxyServer proxyServer) {
        this.logger = logger;
        this.proxyServer = proxyServer;
        try {
            this.backend = new BackendWrapper(BackendConfiguration.getPulsarURL(), this.logger, false, this.proxyServer::shutdown, uuid -> proxyServer.getPlayer(uuid).isPresent());
        } catch (Exception exception) {
            this.proxyServer.shutdown();
            throw new RuntimeException("Unexpected exception", exception);
        }
        if (isOffline()) {
            logger.warn("Running in offline mode!");
        }
    }

    @Subscribe
    public void onProxyInitializeEvent(ProxyInitializeEvent event) {
        try {
            this.backend.start();
            EventManager eventManager = this.proxyServer.getEventManager();
            eventManager.register(this, new PlayerLogin(this));
        } catch (Exception exception) {
            this.logger.error("Cannot start backend", exception);
            this.proxyServer.shutdown();
        }
    }

    @Subscribe (order = PostOrder.LAST)
    public void onProxyShutdownEvent(ProxyShutdownEvent event) {
        try {
            this.backend.shutdown();
        } catch (Exception exception) {
            this.logger.error("Cannot shutdown backend!", exception);
        }
    }

    public static boolean isOffline() {
        return BackendConfiguration.isOffline();
    }
}
