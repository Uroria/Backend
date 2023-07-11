package com.uroria.backend.velocity;

import com.uroria.backend.common.server.BackendServer;
import com.uroria.backend.common.server.ServerStatus;
import com.uroria.backend.server.AbstractServerManager;
import com.uroria.backend.server.BackendAllServersRequest;
import com.uroria.backend.server.BackendServerRequest;
import com.uroria.backend.server.BackendServerStart;
import com.uroria.backend.server.BackendServerUpdate;
import com.uroria.backend.velocity.events.ServerStartEvent;
import com.uroria.backend.velocity.events.ServerUpdateEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.*;

public final class ServerManagerImpl extends AbstractServerManager {
    private final ProxyServer proxyServer;
    private BackendServerRequest request;
    private BackendServerUpdate update;
    private BackendServerStart start;
    private BackendAllServersRequest requestAll;
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
            this.requestAll = new BackendAllServersRequest(this.pulsarClient, identifier);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void shutdown() {
        try {
            if (this.request != null) this.request.close();
            if (this.update != null) this.update.close();
            if (this.start != null) this.start.close();
            if (this.requestAll != null) this.requestAll.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            BackendAPIImpl.captureException(exception);
        }
    }

    @Override
    protected void checkServer(BackendServer server) {
        if (this.servers.stream().noneMatch(server::equals)) {
            if (server.getStatus() == ServerStatus.CLOSED || server.getStatus() == ServerStatus.STOPPED) {
                this.logger.info("Server " + server.getDisplayName() + " doesn't get updated because stopped");
                return;
            }
        }

        logger.info("Updating server " + server.getDisplayName() + " " + Arrays.toString(this.servers.stream().map(BackendServer::getDisplayName).toArray()));

        for (BackendServer savedServer : this.servers) {
            if (!savedServer.equals(server)) continue;
            savedServer.modify(server);

            this.proxyServer.getEventManager().fireAndForget(new ServerUpdateEvent(savedServer));

            if (savedServer.getStatus() == ServerStatus.STOPPED) {
                this.servers.remove(savedServer);
                this.logger.info("Removing server " + savedServer.getDisplayName());
            }

            return;
        }

        this.logger.info("Adding server " + server.getDisplayName());
        this.servers.add(server);
        this.proxyServer.getEventManager().fireAndForget(new ServerUpdateEvent(server));
    }

    @Override
    public Optional<BackendServer> getServer(int id, int timeout) {
        for (BackendServer server : this.servers) {
            if (server.getId().isEmpty()) continue;
            if (server.getId().get().equals(id)) return Optional.of(server);
        }

        return this.request.request(id, timeout);
    }

    @Override
    public Collection<BackendServer> getServers() {
        int randomInteger = new Random().nextInt();
        List<Integer> serverIds = this.requestAll.request(randomInteger, 3000).orElseThrow(() -> new RuntimeException("Cannot get server list"));
        return serverIds.stream().map(id -> getServer(id).orElse(null)).filter(Objects::nonNull).toList();
    }

    @Override
    public BackendServer updateServer(@NonNull BackendServer server) {
        if (server.getId().isEmpty()) throw new IllegalStateException("Server not created yet");
        try {
            checkServer(server);
            this.update.update(server);
        } catch (Exception exception) {
            this.logger.error("Cannot update server", exception);
            BackendAPIImpl.captureException(exception);
        }
        return server;
    }

    @Override
    public BackendServer startServer(@NonNull BackendServer server) {
        if (server.getId().isPresent()) throw new IllegalStateException("Server already started");
        try {
            Optional<BackendServer> request = this.start.request(server, 3000);
            if (request.isEmpty()) return null;
            server = request.get();
        } catch (Exception exception) {
            this.logger.error("Cannot start server", exception);
            BackendAPIImpl.captureException(exception);
        }

        this.proxyServer.getEventManager().fireAndForget(new ServerStartEvent(server));
        return server;
    }
}
