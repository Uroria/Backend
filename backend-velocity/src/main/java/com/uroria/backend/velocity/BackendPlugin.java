package com.uroria.backend.velocity;

import com.google.inject.Inject;
import com.uroria.are.Application;
import com.uroria.backend.Backend;
import com.uroria.backend.WrapperEnvironment;
import com.uroria.backend.app.ApplicationStatus;
import com.uroria.backend.cache.communication.proxy.ProxyPing;
import com.uroria.backend.communication.broadcast.Broadcaster;
import com.uroria.backend.impl.AbstractBackendWrapper;
import com.uroria.backend.impl.BackendInitializer;
import com.uroria.backend.impl.BackendWrapperImpl;
import com.uroria.backend.impl.utils.WrapperUtils;
import com.uroria.backend.proxy.Proxy;
import com.uroria.backend.proxy.events.ProxyDeletedEvent;
import com.uroria.backend.proxy.events.ProxyUpdatedEvent;
import com.uroria.backend.velocity.perm.BackendPermissionProvider;
import com.uroria.base.event.EventManager;
import com.uroria.base.event.Listener;
import com.uroria.base.utils.ThreadUtils;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "backend",
        name = "Backend",
        authors = "Verklickt"
)
public final class BackendPlugin {
    private final Logger logger;
    private final ProxyServer proxyServer;
    private final AbstractBackendWrapper wrapper;
    @Getter
    private final BackendPermissionProvider permissionProvider;
    private Proxy proxy;
    private Broadcaster<ProxyPing> ping;

    @Inject
    public BackendPlugin(Logger logger, ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
        this.permissionProvider = new BackendPermissionProvider(logger);
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
            proxyServer.getEventManager().register(this, new Listeners(this.wrapper, this));
            if (Application.isOffline()) {
                this.logger.warn("Running in offline mode. No connections will be established");
                serverSetupIfOffline();
                return;
            }
            WrapperEnvironment environment = Backend.environment();
            this.proxy = environment.getProxy().orElse(null);
            if (this.proxy == null) {
                int templateId = environment.getTemplateId().orElseThrow(() -> new IllegalStateException("Template and proxy id were never assigned"));
                String groupName = environment.getProxyName().orElseThrow(() -> new IllegalStateException("Group and proxy id were never assigned besides templateId"));
                this.proxy = Backend.wrapper().createProxy(groupName, templateId, 999).get();
                if (proxy == null) throw new IllegalStateException("Proxy still null after registration");
            }
            EventManager eventManager = this.wrapper.getEventManager();
            eventManager.subscribe(new Listener<>(ProxyUpdatedEvent.class, 1) {
                @Override
                public void onEvent(ProxyUpdatedEvent event) {
                    long id = event.getProxy().getId();
                    if (proxy.getId() != id) return;
                    if (event.getProxy().getStatus() == ApplicationStatus.STOPPED) proxyServer.shutdown();
                }
            });
            eventManager.subscribe(new Listener<>(ProxyDeletedEvent.class, 1) {
                @Override
                public void onEvent(ProxyDeletedEvent event) {
                    long id = event.getProxy().getId();
                    if (proxy.getId() == id) proxyServer.shutdown();
                }
            });
            if (wrapper instanceof BackendWrapperImpl onlineWrapper) {
                this.ping = onlineWrapper.getProxyManager().getBroadcastPoint().registerBroadcaster(ProxyPing.class, "Ping");
                CompletableFuture.runAsync(() -> {
                    while (true) {
                        if (this.proxy.getStatus() == ApplicationStatus.STOPPED || this.proxy.isDeleted()) {
                            ping.broadcast(new ProxyPing(this.proxy.getId(), System.currentTimeMillis(), true));
                            return;
                        }
                        ping.broadcast(new ProxyPing(this.proxy.getId(), System.currentTimeMillis(), false));
                        ThreadUtils.sleep(1, TimeUnit.SECONDS);
                    }
                });
            }
            this.proxy.setStatus(ApplicationStatus.ONLINE);
        } catch (Exception exception) {
            this.logger.error("Cannot start backend", exception);
            this.proxyServer.shutdown();
        }
    }

    @SuppressWarnings("WarningMarkers")
    private void serverSetupIfOffline() {
        this.proxy = Backend.environment().getProxy().orElse(null);
        if (proxy == null) {
            proxy = Backend.wrapper().createProxy("offline", 0, 999).get();
            if (proxy == null) throw new RuntimeException("Unable to setup proxy");
        }
        this.proxy.setStatus(ApplicationStatus.ONLINE);
    }

    @Subscribe (order = PostOrder.LAST)
    public void onProxyShutdownEvent(ProxyShutdownEvent event) {
        try {
            if (this.proxy != null) {
                this.proxy.delete();
                if (this.ping != null) {
                    this.ping.broadcast(new ProxyPing(this.proxy.getId(), System.currentTimeMillis(), true));
                }
            }
            this.wrapper.shutdown();
        } catch (Exception exception) {
            this.logger.error("Cannot shutdown backend", exception);
        }
    }
}
