package com.uroria.backend.velocity.server;

import com.uroria.backend.impl.server.AbstractServerManager;
import com.uroria.backend.impl.server.AllServersRequestChannel;
import com.uroria.backend.impl.server.ServerIDRequestChannel;
import com.uroria.backend.impl.server.ServerRequestChannel;
import com.uroria.backend.impl.server.ServerStartChannel;
import com.uroria.backend.impl.server.ServerUpdateChannel;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.server.ServerStatus;
import com.uroria.backend.velocity.BackendVelocityPlugin;
import com.velocitypowered.api.proxy.ProxyServer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public final class ServerManagerImpl extends AbstractServerManager implements ServerManager {
    private final ProxyServer proxyServer;
    private ServerRequestChannel request;
    private ServerIDRequestChannel idRequest;
    private ServerUpdateChannel update;
    private AllServersRequestChannel requestAll;
    private ServerStartChannel start;

    public ServerManagerImpl(PulsarClient pulsarClient, Logger logger, ProxyServer proxyServer) {
        super(pulsarClient, logger);
        this.proxyServer = proxyServer;
    }

    @Override
    public void start(String identifier) throws PulsarClientException {
        this.request = new ServerRequestChannel(this.pulsarClient, identifier);
        this.idRequest = new ServerIDRequestChannel(this.pulsarClient, identifier);
        this.update = new ServerUpdateChannel(this.pulsarClient, identifier, this::checkServer);
        this.requestAll = new AllServersRequestChannel(this.pulsarClient, identifier);
        this.start = new ServerStartChannel(this.pulsarClient, identifier);
    }

    @Override
    public void shutdown() throws PulsarClientException {
        if (this.request != null) this.request.close();
        if (this.idRequest != null) this.idRequest.close();
        if (this.update != null) this.update.close();
        if (this.requestAll != null) this.requestAll.close();
        if (this.start != null) this.start.close();
    }

    @Override
    protected void checkServer(@NonNull Server server) {
        if (this.servers.stream().noneMatch(server::equals)) {
            this.logger.info("Adding " + server);
            this.servers.add(server);
        }

        if (server.isDeleted()) {
            this.servers.remove(server);
            this.proxyServer.getEventManager().fireAndForget(new ServerUpdateEvent(server));
            return;
        }

        for (Server cachedServer : this.servers) {
            if (!cachedServer.equals(server)) continue;
            cachedServer.modify(server);

            logger.info("Updated " + server);

            this.proxyServer.getEventManager().fireAndForget(new ServerUpdateEvent(cachedServer));

            switch (server.getStatus()) {
                case STARTING -> {
                    if (cachedServer.getStatus() == ServerStatus.EMPTY) {
                        this.proxyServer.getEventManager().fireAndForget(new ServerStartEvent(cachedServer));
                        return;
                    }
                }
                case CLOSED -> {
                    if (cachedServer.getStatus() == ServerStatus.ENDING) {
                        this.proxyServer.getEventManager().fireAndForget(new ServerCloseEvent(cachedServer));
                        return;
                    }
                }
                case STOPPED -> {
                    if (cachedServer.getStatus() == ServerStatus.CLOSED) {
                        this.proxyServer.getEventManager().fireAndForget(new ServerStopEvent(cachedServer));
                        return;
                    }
                }
            }
            return;
        }
    }

    @Override
    public Optional<Server> getServer(long identifier, int timeout) {
        for (Server server : this.servers) {
            if (server.getIdentifier() == identifier) return Optional.of(server);
        }

        if (BackendVelocityPlugin.isOffline()) return Optional.empty();

        Optional<Server> request = this.request.request(identifier, timeout);
        request.ifPresent(this.servers::add);
        return request;
    }

    @Override
    public Optional<Server> getCloudServer(int id, int timeout) {
        for (Server server : this.servers) {
            if (server.getID() == id) return Optional.of(server);
        }

        if (BackendVelocityPlugin.isOffline()) return Optional.empty();

        Optional<Server> request = this.idRequest.request(id, timeout);
        request.ifPresent(this.servers::add);
        return request;
    }

    @Override
    public List<Server> getServers() {
        if (BackendVelocityPlugin.isOffline()) return new ObjectArrayList<>();

        Optional<List<Server>> request = this.requestAll.request(1, 20000);
        return request.orElse(new ObjectArrayList<>());
    }

    @Override
    public Server startServer(@NonNull Server server) throws IllegalStateException {
        if (server.getID() != -1) throw new IllegalStateException("Server already started");
        try {
            if (BackendVelocityPlugin.isOffline()) return server;
            checkServer(server);
            Optional<Server> request = this.start.request(server, 1000000);
            Server server1 = request.orElse(null);
            if (server1 != null) {
                checkServer(server1);
            }
            server = server1;
        } catch (Exception exception) {
            this.logger.error("Cannot start server ", exception);
        }

        return server;
    }

    @Override
    public void updateServer(@NonNull Server server) {
        if (server.getID() == -1 && !server.isDeleted()) throw new IllegalStateException("Server was never started");
        try {
            checkServer(server);
            if (BackendVelocityPlugin.isOffline()) return;
            this.update.update(server);
        } catch (Exception exception) {
            this.logger.error("Cannot update server " + server, exception);
        }
    }
}
