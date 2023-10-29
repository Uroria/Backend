package com.uroria.backend.velocity;

import com.google.inject.Inject;
import com.uroria.are.Application;
import com.uroria.backend.Backend;
import com.uroria.backend.WrapperEnvironment;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.impl.BackendInitializer;
import com.uroria.backend.impl.utils.WrapperUtils;
import com.uroria.backend.proxy.Proxy;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Plugin(
        id = "backend",
        name = "Backend",
        authors = "Verklickt"
)
public final class BackendPlugin {
    private final Logger logger;
    private final ProxyServer proxyServer;
    private final AbstractBackendWrapper wrapper;
    private Proxy proxy;

    @Inject
    public BackendPlugin(Logger logger, ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        try {
            this.wrapper = BackendInitializer.initialize();
            this.logger = wrapper.getLogger();
            WrapperUtils.getServerId(logger).ifPresent(id -> wrapper.getEnvironment().setProxyId(id));
            WrapperUtils.getTemplateId(logger).ifPresent(id -> wrapper.getEnvironment().setTemplateId(id));
            WrapperUtils.getGroupName().ifPresent(name -> wrapper.getEnvironment().setProxyGroupName(name));
        } catch (Exception exception) {
            LoggerFactory.getLogger("BackendEmergency").error("Unable to initialize", exception);
            proxyServer.shutdown();
            throw exception;
        }
    }

    @Subscribe
    public void onProxyInitializeEvent(ProxyInitializeEvent event) {
        try {
            this.wrapper.start();
            if (!this.wrapper.isStarted())
                throw new IllegalStateException("Wrapper was never started or plugin has been illegally reloaded");
            proxyServer.getEventManager().register(this, new Listeners(this.wrapper));
            if (Application.isOffline()) {
                this.logger.warn("Running in offline mode. No connections will be established");
                serverSetupIfOffline();
                return;
            }
            WrapperEnvironment environment = Backend.getEnvironment();
            this.proxy = environment.getProxy().orElse(null);
            if (this.proxy == null) {
                int templateId = environment.getTemplateId().orElseThrow(() -> new IllegalStateException("Template and proxy id were never assigned"));
                String groupName = environment.getProxyName().orElseThrow(() -> new IllegalStateException("Group and proxy id were never assigned besides templateId"));
                this.proxy = Backend.createProxy(groupName, templateId, 999).get();
                if (proxy == null) throw new IllegalStateException("Proxy still null after registration");
            }
            this.proxy.setStatus(ApplicationStatus.ONLINE);
        } catch (Exception exception) {
            this.logger.error("Cannot start backend", exception);
            this.proxyServer.shutdown();
        }
    }

    @SuppressWarnings("WarningMarkers")
    private void serverSetupIfOffline() {
        this.proxy = Backend.getEnvironment().getProxy().orElse(null);
        if (proxy == null) {
            proxy = Backend.createProxy("offline", 0, 999).get();
            if (proxy == null) throw new RuntimeException("Unable to setup proxy");
        }
        this.proxy.setStatus(ApplicationStatus.ONLINE);
    }

    @Subscribe (order = PostOrder.LAST)
    public void onProxyShutdownEvent(ProxyShutdownEvent event) {
        try {
            if (this.proxy != null) {
                this.proxy.delete();
            }
            this.wrapper.shutdown();
        } catch (Exception exception) {
            this.logger.error("Cannot shutdown backend", exception);
        }
    }
}
