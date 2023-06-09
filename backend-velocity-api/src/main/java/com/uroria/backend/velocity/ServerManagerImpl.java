package com.uroria.backend.velocity;

import com.uroria.backend.common.BackendServer;
import com.uroria.backend.server.BackendServerRequest;
import com.uroria.backend.server.BackendServerStart;
import com.uroria.backend.server.BackendServerUpdate;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.velocity.events.ServerStartEvent;
import com.uroria.backend.velocity.events.ServerUpdateEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.Optional;

public final class ServerManagerImpl extends ServerManager {
    private final ProxyServer proxyServer;
    private BackendServerRequest request;
    private BackendServerUpdate update;
    private BackendServerStart start;
    public ServerManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    protected void start(String identifier) {
        try {
            this.request = new BackendServerRequest(this.pulsarClient, identifier);
            this.update = new BackendServerUpdate(this.pulsarClient, identifier, this::checkServer);
            this.start = new BackendServerStart(this.pulsarClient, identifier);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            BackendAPI.captureException(exception);
        }
    }

    @Override
    protected void shutdown() {
        try {
            if (this.request != null) this.request.close();
            if (this.update != null) this.update.close();
            if (this.start != null) this.start.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPI.captureException(exception);
        }
    }

    @Override
    protected void checkServer(BackendServer server) {
        this.servers.removeIf(server1 -> server1.getId() == server.getId());
        this.servers.add(server);
        this.proxyServer.getEventManager().fireAndForget(new ServerUpdateEvent(server));
    }

    @Override
    public Optional<BackendServer> getServer(int id, int timeout) {
        for (BackendServer server : this.servers) {
            if (server.getId().isEmpty()) continue;
            if (server.getId().get().equals(id)) return Optional.of(server);
        }

        return this.request.request(id);
    }

    @Override
    public void updateServer(BackendServer server) {
        if (server == null) throw new NullPointerException("Server cannot be null");
        if (server.getId().isEmpty()) throw new IllegalStateException("Server not created yet");
        try {
            checkServer(server);
            this.update.update(server);
        } catch (Exception exception) {
            this.logger.error("Cannot update server", exception);
            BackendAPI.captureException(exception);
        }
    }

    @Override
    public BackendServer startServer(BackendServer server) {
        if (server == null) throw new NullPointerException("Server cannot be null");
        if (server.getId().isPresent()) throw new IllegalStateException("Server already started");
        try {
            Optional<BackendServer> request = this.start.request(server);
            if (request.isEmpty()) return null;
            server = request.get();
        } catch (Exception exception) {
            this.logger.error("Cannot start server", exception);
            BackendAPI.captureException(exception);
        }

        this.proxyServer.getEventManager().fireAndForget(new ServerStartEvent(server));
        return server;
    }
}
