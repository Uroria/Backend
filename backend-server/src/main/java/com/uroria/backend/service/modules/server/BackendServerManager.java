package com.uroria.backend.service.modules.server;

import com.uroria.backend.impl.configuration.BackendConfiguration;
import com.uroria.backend.server.ServerStatus;
import com.uroria.backend.service.BackendServer;
import com.uroria.backend.service.modules.AbstractManager;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import lombok.NonNull;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;

public final class BackendServerManager extends AbstractManager implements ServerManager {
    private final ObjectArraySet<Serverold> servers;
    private final CloudAPI cloudAPI;
    private ServerUpdate update;
    private ServerStart start;
    private ServerResponse response;
    private ServerIDResponse idResponse;
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
        this.idResponse = new ServerIDResponse(this.pulsarClient, this);
        this.allResponse = new ServerAllResponse(this.pulsarClient, this);
        this.keepAlive = new ServerKeepAlive(this.pulsarClient, this);
    }

    @Override
    protected void disable() throws PulsarClientException {
        if (this.update != null) this.update.close();
        if (this.start != null) this.start.close();
        if (this.response != null) this.response.close();
        if (this.idResponse != null) this.idResponse.close();
        if (this.allResponse != null) this.allResponse.close();
        if (this.keepAlive != null) this.keepAlive.close();
        try {
            this.cloudAPI.close();
        } catch (Exception exception) {
            throw new RuntimeException("Something went wrong", exception);
        }
    }

    @Override
    public Optional<Serverold> getServer(long identifier, int timeout) {
        for (Serverold server : this.servers) {
            if (server.getIdentifier() == identifier) return Optional.of(server);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Serverold> getCloudServer(int id, int timeout) {
        for (Serverold server : this.servers){
            if (server.getID() == id) return Optional.of(server);
        }
        return Optional.empty();
    }

    @Override
    public Serverold startServer(@NonNull Serverold server) {
        if (server.getStatus() != ServerStatus.EMPTY) return null;
        try {
            int id = this.cloudAPI.startServer(server.getTemplateID());
            Unsafe.setServerID(server, id);
            server.setStatus(ServerStatus.STARTING);
            updateServer(server);
            InetSocketAddress address = this.cloudAPI.getAddress(id, 600000);
            if (address == null) {
                server.setStatus(ServerStatus.STOPPED);
                updateServer(server);
                BackendServer.getLogger().info("Cannot receive address of Cloud for " + id);
                return null;
            }
            server.setAddress(address);
            updateServer(server);
            this.logger.info("Starting server " + server.getDisplayName() + " on " + address.getHostName() + ":" + address.getPort());
            return server;
        } catch (Exception exception) {
            this.logger.error("Unhandled exception", exception);
            return null;
        }
    }

    @Override
    public void updateServer(@NonNull Serverold server) {
        updateLocal(server);
        this.update.update(server);
    }

    void updateLocal(@NonNull Serverold server) {
        if (server.getID() == -1 && !server.isDeleted()) throw new IllegalStateException("Server not started yet");
        if (server.isDeleted()) server.setStatus(ServerStatus.STOPPED);
        if (this.servers.stream().noneMatch(server::equals)) {
            switch (server.getStatus()) {
                case STOPPED, CLOSED -> {
                    this.servers.removeIf(server::equals);
                    return;
                }
            }
        }

        for (Serverold savedServer : this.servers) {
            if (!savedServer.equals(server)) continue;
            savedServer.modify(server);

            this.logger.info("Updated " + savedServer);

            return;
        }

        this.logger.info("Added " + server);
        this.servers.add(server);
    }

    @Override
    public List<Serverold> getServers() {
        return this.servers.stream().toList();
    }
}
