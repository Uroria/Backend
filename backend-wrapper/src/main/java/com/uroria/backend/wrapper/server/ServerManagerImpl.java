package com.uroria.backend.wrapper.server;

import com.uroria.backend.impl.server.AbstractServerManager;
import com.uroria.backend.impl.server.AllServersRequestChannel;
import com.uroria.backend.impl.server.ServerIDRequestChannel;
import com.uroria.backend.impl.server.ServerRequestChannel;
import com.uroria.backend.impl.server.ServerStartChannel;
import com.uroria.backend.impl.server.ServerUpdateChannel;
import com.uroria.backend.server.ServerStatus;
import com.uroria.base.event.EventManager;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

public final class ServerManagerImpl extends AbstractServerManager implements ServerManager {
    private final boolean offline;
    private final EventManager eventManager;
    private ServerRequestChannel request;
    private ServerIDRequestChannel idRequest;
    private ServerUpdateChannel update;
    private AllServersRequestChannel requestAll;
    private ServerStartChannel start;

    public ServerManagerImpl(PulsarClient pulsarClient, Logger logger, boolean offline, EventManager eventManager) {
        super(pulsarClient, logger);
        this.offline = offline;
        this.eventManager = eventManager;
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
    protected void checkServer(@NonNull Serverold server) {
        if (this.servers.stream().noneMatch(server::equals)) {
            this.logger.info("Adding " + server);
            this.servers.add(server);
        }

        if (server.isDeleted()) {
            for (Serverold cachedServer : this.servers) {
                if (cachedServer.equals(server)) {
                    cachedServer.delete();
                }
            }
            this.servers.remove(server);
            this.eventManager.callAndForget(new ServerUpdateEvent(server));
            return;
        }

        for (Serverold cachedServer : this.servers) {
            if (!cachedServer.equals(server)) continue;
            cachedServer.modify(server);

            logger.info("Updated " + server);

            this.eventManager.callAndForget(new ServerUpdateEvent(cachedServer));

            switch (server.getStatus()) {
                case STARTING -> {
                    if (cachedServer.getStatus() == ServerStatus.EMPTY) {
                        this.eventManager.callAndForget(new ServerStartEvent(cachedServer));
                        return;
                    }
                }
                case CLOSED -> {
                    if (cachedServer.getStatus() == ServerStatus.ENDING) {
                        this.eventManager.callAndForget(new ServerCloseEvent(cachedServer));
                        return;
                    }
                }
                case STOPPED -> {
                    cachedServer.delete();
                    if (cachedServer.getStatus() == ServerStatus.CLOSED) {
                        this.eventManager.callAndForget(new ServerStopEvent(cachedServer));
                        return;
                    }
                }
            }
            return;
        }
    }

    @Override
    public Optional<Serverold> getServer(long identifier, int timeout) {
        for (Serverold server : this.servers) {
            if (server.getIdentifier() == identifier) return Optional.of(server);
        }

        if (this.offline) return Optional.empty();

        Optional<Serverold> request = this.request.request(identifier, timeout);
        request.ifPresent(this.servers::add);
        return request;
    }

    @Override
    public Optional<Serverold> getCloudServer(int id, int timeout) {
        for (Serverold server : this.servers) {
            if (server.getID() == id) return Optional.of(server);
        }

        if (this.offline) return Optional.empty();

        Optional<Serverold> request = this.idRequest.request(id, timeout);
        request.ifPresent(this.servers::add);
        return request;
    }

    @Override
    public List<Serverold> getServers() {
        if (this.offline) return new ObjectArrayList<>();

        Optional<List<Serverold>> request = this.requestAll.request(1, 20000);
        return request.orElse(new ObjectArrayList<>());
    }

    @Override
    public Serverold startServer(@NonNull Serverold server) throws IllegalStateException {
        if (server.getID() != -1) throw new IllegalStateException("Server already started");
        try {
            if (this.offline) return server;
            checkServer(server);
            Optional<Serverold> request = this.start.request(server, 1000000);
            Serverold server1 = request.orElse(null);
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
    public void updateServer(@NonNull Serverold server) {
        if (server.getID() == -1 && !server.isDeleted()) throw new IllegalStateException("Server was never started");
        try {
            checkServer(server);
            if (this.offline) return;
            this.update.update(server);
        } catch (Exception exception) {
            this.logger.error("Cannot update server " + server, exception);
        }
    }
}
