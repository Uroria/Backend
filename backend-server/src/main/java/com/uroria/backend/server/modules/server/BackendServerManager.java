package com.uroria.backend.server.modules.server;

import com.uroria.backend.server.ServerManager;
import com.uroria.backend.server.Unsafe;
import com.uroria.backend.pluginapi.BackendRegistry;
import com.uroria.backend.pluginapi.events.server.ServerStartEvent;
import com.uroria.backend.pluginapi.events.server.ServerStopEvent;
import com.uroria.backend.pluginapi.events.server.ServerUpdateEvent;
import com.uroria.backend.server.BackendServer;
import com.uroria.backend.server.ServerStatus;
import com.uroria.backend.server.CloudAPI;
import com.uroria.backend.server.Uroria;
import com.uroria.backend.server.events.BackendEventManager;
import com.uroria.backend.server.modules.AbstractManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.Set;

public final class BackendServerManager extends AbstractManager implements ServerManager {
    private final PulsarClient pulsarClient;
    private final BackendEventManager eventManager;
    private final CloudAPI api;
    private final Set<BackendServer> servers;
    private BackendServerResponse serverResponse;
    private BackendServerUpdate serverUpdate;
    private BackendAllServersResponse allServersResponse;
    private BackendServerStartAcknowledge startAcknowledge;
    private BackendServerKeepAlive keepAlive;

    public BackendServerManager(Logger logger, PulsarClient pulsarClient, CloudAPI api) {
        super(logger, "ServerModule");
        this.pulsarClient = pulsarClient;
        this.eventManager = BackendRegistry.get(BackendEventManager.class).orElseThrow(() -> new NullPointerException("EventManager not initialized"));
        this.api = api;
        this.servers = ObjectSets.synchronize(new ObjectArraySet<>());
    }

    @Override
    public void enable() {
        try {
            this.serverResponse = new BackendServerResponse(this.pulsarClient, this);
            this.serverUpdate = new BackendServerUpdate(this.pulsarClient, this);
            this.startAcknowledge = new BackendServerStartAcknowledge(this.pulsarClient, this);
            this.allServersResponse = new BackendAllServersResponse(this.pulsarClient, this);
            this.keepAlive = new BackendServerKeepAlive(this.pulsarClient, this);
            this.keepAlive.start();
        } catch (Exception exception) {
            this.logger.error("Cannot initialize handlers", exception);
            Uroria.captureException(exception);
        }
    }

    @Override
    public void disable() {
        for (BackendServer server : this.servers) {
            try {
                this.logger.info("Shutting down server " + server.getDisplayName());
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
            if (this.allServersResponse != null) this.allServersResponse.close();
            if (this.keepAlive != null) this.keepAlive.close();
        } catch (Exception exception) {
            this.logger.error("Cannot close handlers", exception);
            Uroria.captureException(exception);
        }
    }

    @Override
    public Optional<BackendServer> getServer(int id, int timeout) {
        return getServer(id);
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
    public BackendServer updateServer(@NotNull BackendServer server) {
        this.serverUpdate.update(server);
        updateLocal(server);
        return server;
    }

    void updateLocal(BackendServer server) {
        if (server.getId().isEmpty()) throw new IllegalStateException("Server not started yet");
        if (this.servers.stream().noneMatch(server::equals)) {
            if (server.getStatus() == ServerStatus.CLOSED || server.getStatus() == ServerStatus.STOPPED) return;
        }

        for (BackendServer savedServer : this.servers) {
            if (!savedServer.equals(server)) continue;
            savedServer.modify(server);

            logger.info("Updating server " + savedServer.getDisplayName());

            this.eventManager.callEventAsync(new ServerUpdateEvent(savedServer));

            ServerStatus currentStatus = savedServer.getStatus();
            ServerStatus nextStatus = server.getStatus();
            if (nextStatus == ServerStatus.STARTING && currentStatus == ServerStatus.EMPTY) this.eventManager.callEventAsync(new ServerStartEvent(server));
            if (nextStatus == ServerStatus.STOPPED && currentStatus == ServerStatus.CLOSED) this.eventManager.callEventAsync(new ServerStopEvent(server));
            if (currentStatus == ServerStatus.STOPPED) {
                this.servers.remove(savedServer);
                logger.info("Removing server " + savedServer.getDisplayName());
            }
            return;
        }

        logger.info("Adding server " + server.getDisplayName());
        this.servers.add(server);
    }

    @Override
    public BackendServer startServer(@NonNull BackendServer server) {
        if (server.getStatus() != ServerStatus.EMPTY) return null;
        try {
            int id = this.api.startServer(server.getTemplateId());
            Unsafe.setIdOfServer(server, id);
            server.setStatus(ServerStatus.STARTING);
            updateServer(server);
            InetSocketAddress address = this.api.getAddress(id, 1000000);
            server.setProperty("address", address);
            updateServer(server);
            this.logger.info("Starting server " + server.getDisplayName() + " on " + address.getHostName() + ":" + address.getPort());

            return server;
        } catch (Exception exception) {
            this.logger.error("Cannot start server", exception);
            Uroria.captureException(exception);
            return null;
        }
    }

    @Override
    public Set<BackendServer> getServers() {
        return this.servers;
    }

    public BackendServer getServer(long identifier) {
        return this.servers.stream().filter(server -> server.getIdentifier() == identifier).findFirst().orElse(null);
    }
}
