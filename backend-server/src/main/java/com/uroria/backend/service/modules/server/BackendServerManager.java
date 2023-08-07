package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.configuration.BackendConfiguration;
import com.uroria.backend.server.Server;
import com.uroria.backend.server.ServerManager;
import com.uroria.backend.server.ServerStatus;
import com.uroria.backend.server.Unsafe;
import com.uroria.backend.service.modules.AbstractManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

public final class BackendServerManager extends AbstractManager implements ServerManager {
    private final ObjectArraySet<Server> servers;
    private final CloudAPI cloudAPI;
    private ServerUpdate update;
    private ServerStart start;
    private ServerResponse response;
    private ServerAllResponse allResponse;
    private ServerKeepAlive keepAlive;

    public BackendServerManager(PulsarClient pulsarClient) {
        super(pulsarClient, "ServerModule");
        this.cloudAPI = new CloudAPI(BackendConfiguration.getString("cloud.uuid"), BackendConfiguration.getString("cloud.token"));
        this.servers = new ObjectArraySet<>();
    }

    @Override
    protected void enable() throws PulsarClientException {
        this.update = new ServerUpdate(this.pulsarClient, this);
        this.start = new ServerStart(this.pulsarClient, this);
        this.response = new ServerResponse(this.pulsarClient, this);
        this.allResponse = new ServerAllResponse(this.pulsarClient, this);
        this.keepAlive = new ServerKeepAlive(this.pulsarClient, this);
    }

    @Override
    protected void disable() throws PulsarClientException {
        if (this.update != null) this.update.close();
        if (this.start != null) this.start.close();
        if (this.response != null) this.response.close();
        if (this.allResponse != null) this.allResponse.close();
        if (this.keepAlive != null) this.keepAlive.close();
    }

    @Override
    public Optional<Server> getServer(long identifier, int timeout) {
        for (Server server : this.servers) {
            if (server.getIdentifier() == identifier) return Optional.of(server);
        }
        return Optional.empty();
    }

    @Override
    public Server startServer(@NonNull Server server) {
        if (server.getStatus() != ServerStatus.EMPTY) return null;
        try {
            int id = this.cloudAPI.startServer(server.getTemplateID());
            Unsafe.setServerID(server, id);
            server.setStatus(ServerStatus.STARTING);
            updateServer(server);
            InetSocketAddress address = null; //this.cloudAPI.getAddress(id, 1000000);
            server.setProperty("address", address);
            updateServer(server);
            this.logger.info("Starting server " + server.getDisplayName() + " on " + address.getHostName() + ":" + address.getPort());
            return server;
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            return null;
        }
    }

    @Override
    public void updateServer(@NonNull Server server) {
        updateLocal(server);
        this.update.update(server);
    }

    void updateLocal(@NonNull Server server) {
        if (server.getID() == -1) throw new IllegalStateException("Server not started yet");
        if (this.servers.stream().noneMatch(server::equals)) {
            switch (server.getStatus()) {
                case STOPPED, CLOSED -> {
                    return;
                }
            }
        }

        for (Server savedServer : this.servers) {
            if (!savedServer.equals(server)) continue;
            savedServer.modify(server);

            this.logger.info("Updated " + savedServer);

            return;
        }

        this.logger.info("Added " + server);
        this.servers.add(server);
    }

    @Override
    public List<Server> getServers() {
        return this.servers.stream().toList();
    }
}