package com.uroria.backend.server.modules.server;

import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.server.ServerStartEvent;
import com.uroria.backend.pluginapi.events.server.ServerStopEvent;
import com.uroria.backend.pluginapi.events.server.ServerUpdateEvent;
import com.uroria.backend.pluginapi.modules.ServerManager;
import com.uroria.backend.common.BackendServer;
import com.uroria.backend.common.Unsafe;
import com.uroria.backend.common.helpers.ServerStatus;
import com.uroria.backend.server.CloudAPI;
import com.uroria.backend.server.Uroria;
import com.uroria.backend.server.events.BackendEventManager;
import org.apache.pulsar.client.api.PulsarClient;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class BackendServerManager implements ServerManager {
    private final Logger logger;
    private final PulsarClient pulsarClient;
    private final BackendEventManager eventManager;
    private final CloudAPI api;
    private final Collection<BackendServer> servers;
    private BackendServerResponse serverResponse;
    private BackendServerUpdate serverUpdate;
    private BackendServerStartAcknowledge startAcknowledge;

    public BackendServerManager(Logger logger, PulsarClient pulsarClient, CloudAPI api) {
        this.logger = logger;
        this.pulsarClient = pulsarClient;
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
        this.api = api;
        this.servers = new CopyOnWriteArrayList<>();
    }

    public void start() {
        try {
            this.serverResponse = new BackendServerResponse(this.pulsarClient, this);
            this.serverUpdate = new BackendServerUpdate(this.pulsarClient, this);
            this.startAcknowledge = new BackendServerStartAcknowledge(this.pulsarClient, this);
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
        }
    }

    public void shutdown() {
        for (BackendServer server : this.servers) {
            try {
                server.setStatus(ServerStatus.STOPPED);
                this.serverUpdate.update(server);
            } catch (Exception exception) {
                this.logger.error("Cannot shutdown server " + server.getId(), exception);
                Uroria.captureException(exception);
            }
        }

        try {
            if (this.serverResponse != null) this.serverResponse.close();
            if (this.serverUpdate != null) this.serverUpdate.close();
            if (this.startAcknowledge != null) this.startAcknowledge.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            Uroria.captureException(exception);
        }
    }

    @Override
    public Optional<BackendServer> getServer(int id) {
        for (BackendServer server : this.servers) {
            if (server.getId().isEmpty()) continue;
            if (server.getId().get().equals(id)) return Optional.of(server);
        }
        return Optional.empty();
    }

    @Override
    public void updateServer(BackendServer server) {
        updateLocal(server);
        this.serverUpdate.update(server);
    }

    void updateLocal(BackendServer server) {
        if (server.getId().isEmpty()) throw new IllegalStateException("Server not started yet");

        for (BackendServer backendServer : this.servers) {
            if (backendServer.getId().isEmpty()) continue;
            if (!backendServer.getId().get().equals(server.getId().get())) continue;
            if (server.getStatus() == backendServer.getStatus()) break;
            ServerStatus currentStatus = backendServer.getStatus();
            ServerStatus nextStatus = server.getStatus();
            if (nextStatus == ServerStatus.STARTING && currentStatus == ServerStatus.EMPTY) this.eventManager.callEventAsync(new ServerStartEvent(server));
            if (nextStatus == ServerStatus.STOPPED && currentStatus == ServerStatus.CLOSED) this.eventManager.callEventAsync(new ServerStopEvent(server));

            this.servers.remove(backendServer);
            break;
        }
        this.servers.add(server);
        this.eventManager.callEventAsync(new ServerUpdateEvent(server));
    }

    @Override
    public BackendServer startServer(BackendServer server) {
        if (server.getStatus() != ServerStatus.EMPTY) return null;
        try {
            int id = this.api.startServer(server.getTemplateId(), "http://rpr.api.uroria.com:8004/api/v1");
            Unsafe.setIdOfServer(server, id);
            server.setStatus(ServerStatus.STARTING);
            this.servers.removeIf(server1 -> server1.getId().isPresent() && server1.getId().get().equals(id));
            this.servers.add(server);
            return server;
        } catch (Exception exception) {
            this.logger.error("Cannot start server", exception);
            Uroria.captureException(exception);
            return null;
        }
    }
}
